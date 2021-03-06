package net.kaleidos.comicsmagic.helper.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class RarExtractor{

	public static File getFirstImageFile(File file, Context context) {
		File outputFile = null;
		try {
			File outputDir = context.getCacheDir(); // temp dir
			File thumbnailDir = new File (outputDir.getAbsolutePath() + File.separator + "thumbnail");
			thumbnailDir.mkdir();

			outputFile = new File(thumbnailDir + File.separator + file.getName() + ".jpg");
			File outputFileTmp = new File(thumbnailDir + File.separator + file.getName() + ".tmp");
			if (!outputFile.exists()) {

				Archive arch = new Archive(file);
				FileHeader fh = null;

				while (true) {
					fh = arch.nextFileHeader();
					if (fh == null) {
						break;
					}
					if ((fh != null)&&(!fh.isDirectory())&& (Utils.isSupportedFile(fh.getFileNameString(), AppConstant.IMAGE_EXTN))) {
						OutputStream stream = new FileOutputStream(outputFileTmp);
						arch.extractFile(fh, stream);
						stream.close();

						FileOutputStream fout = new FileOutputStream(outputFile);
						InputStream in = new FileInputStream(outputFileTmp);

						Bitmap imageBitmap = Utils.readBitmapFromStream(in);

						imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 71, 100, false);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						fout.write(baos.toByteArray());
						fout.close();
						in.close();
						imageBitmap.recycle();
						outputFileTmp.delete();
						break;
					}
				}

				arch.close();
			}
		} catch (Exception e) {
			Log.e("Decompress", "unrar", e);
		}
		return outputFile;
	}

	public static ArrayList<String> getAllImagesFile(File file, File outputDir) {
		ArrayList<String> fileNames = new ArrayList<String>();

		try {
			Archive arch = new Archive(file);
			FileHeader fh = null;

			while (true) {
				fh = arch.nextFileHeader();
				if (fh == null) {
					break;
				}
				if ((fh != null)&&(!fh.isDirectory())&& (Utils.isSupportedFile(fh.getFileNameString(), AppConstant.IMAGE_EXTN))) {
					Log.d("Decompress", "DEBUG RAR "+fh.getFileNameString());
					File outputFile = new File (outputDir.getAbsolutePath() + File.separator + fh.getFileNameString());
					OutputStream stream;
					try {
						stream = new FileOutputStream(outputFile);
						arch.extractFile(fh, stream);
						stream.close();
						fileNames.add(outputFile.getAbsolutePath());
					} catch (Exception e) {
						Log.e("Decompress", "Fail on unrar file: "+fh.getFileNameString(), e);
						continue;
					}
				}
			}
			arch.close();
		} catch (Exception e) {
			Log.e("Decompress", "unrar", e);
		}


		return fileNames;

	}






}

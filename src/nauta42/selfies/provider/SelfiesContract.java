package nauta42.selfies.provider;

import android.net.Uri;

public final class SelfiesContract {

	public static final String AUTHORITY = "nauta42.selfies.provider";
	public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY + "/");
	public static final String SELFIES_TABLE_NAME = "selfies";
	// URI for this table
	public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, SELFIES_TABLE_NAME);
	//columns
	public static final String _ID = "_id";
	public static final String SELFIE_BITMAP_PATH = "selfieBitmapPath";
	public static final String SELFIE_FILE_PATH = "selfieFilePath";
	public static final String DATE = "date";
	public static final String TIME = "time";

}

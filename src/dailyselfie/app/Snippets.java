package dailyselfie.app;

public class Snippets {

}

/*
	int rotation =-1;
    long fileSize = new File(filePath).length();

    Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE }, MediaStore.MediaColumns.DATE_ADDED + ">=?", new String[]{String.valueOf(captureTime/1000 - 1)}, MediaStore.MediaColumns.DATE_ADDED + " desc");

    if (mediaCursor != null && captureTime != 0 && mediaCursor.getCount() !=0 ) {
        while(mediaCursor.moveToNext()){
            long size = mediaCursor.getLong(1);
            //Extra check to make sure that we are getting the orientation from the proper file
            if(size == fileSize){
                rotation = mediaCursor.getInt(0);
                break;
            }
        }
    }


*/
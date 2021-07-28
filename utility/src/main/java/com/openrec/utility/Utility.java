package com.openrec.utility;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utility {

    public static SimpleDateFormat InputFullDateFormat(){
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    }

    public static SimpleDateFormat OutputDateFormat12(){
        return new SimpleDateFormat("MMM dd, yyyy | h:mm a", Locale.getDefault());
    }

    public static SimpleDateFormat OutputDateFormat24(){
        return new SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault());
    }

    public static SimpleDateFormat DetailsFormat12(){
        return new SimpleDateFormat("MMM dd, yyyy | h:mm:ss a", Locale.getDefault());
    }

    public static SimpleDateFormat DetailsFormat24(){
        return new SimpleDateFormat("MMM dd, yyyy | HH:mm:ss", Locale.getDefault());
    }
}

package com.nabrothers.psl.sdk.message;

public class CQCode {
    public static final String AT_PATTERN = "[CQ:at,qq=%d]";
    public static final String IMAGE_PATTERN = "[CQ:image,file=%s]";
    public static final String VIDEO_PATTERN = "[CQ:video,file=%s]";
    public static final String AUDIO_PATTERN = "[CQ:tts,text=%s]";
    public static final String FACE_PATTERN = "[CQ:face,id=%d]";
    public static final String REPLY_PATTERN = "[CQ:reply,id=%d]";
    public static final String SHARE_PATTERN = "[CQ:share,url=%s,title=%s,content=%s]";
}

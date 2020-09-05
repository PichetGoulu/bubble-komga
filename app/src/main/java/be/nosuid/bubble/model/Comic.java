package be.nosuid.bubble.model;

import java.io.Serializable;
import java.util.Comparator;

public class Comic implements Serializable {
    private String mId;
    private String mName;
    private String mSeriesId;
    private int mChapterNum;
    private int mPageLastRead;
    private int mPagesCount;

    public Comic(String id, String name, String seriesId, int chapterNum, int pageLastRead, int pagesCount) {
        mId = id;
        mName = name;
        mSeriesId = seriesId;
        mChapterNum = chapterNum;
        mPageLastRead = pageLastRead;
        mPagesCount = pagesCount;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getSeriesId() {
        return mSeriesId;
    }

    public int getChapterNum() {
        return mChapterNum;
    }

    public int getPageLastRead() {
        return mPageLastRead;
    }

    public void setPageLastRead(int pageNum) {
        mPageLastRead = pageNum;
    }

    public int getPagesCount() {
        return mPagesCount;
    }


    @Override
    public boolean equals(Object o) {
        return (o instanceof Comic) && getId().equals(((Comic) o).getId());
    }

    public static class ChapterOrderComparator implements Comparator<Comic> {
        @Override
        public int compare(Comic c1, Comic c2) {
            return c1.getChapterNum() - c2.getChapterNum();
        }
    }

    public static class ChapterReverseOrderComparator implements Comparator<Comic> {
        @Override
        public int compare(Comic c1, Comic c2) {
            return c2.getChapterNum() - c1.getChapterNum();
        }
    }

}

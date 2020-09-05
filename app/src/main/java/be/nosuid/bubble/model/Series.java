package be.nosuid.bubble.model;

import java.io.Serializable;

public class Series implements Serializable {
    private String mId;
    private String mName;
    private int mBookLastRead;
    private int mBooksCount;

    public Series(String id, String name, int bookLastRead, int booksCount) {
        mId = id;
        mName = name;
        mBookLastRead = bookLastRead;
        mBooksCount = booksCount;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getBooksLastRead() {
        return mBookLastRead;
    }

    public int getBooksCount() {
        return mBooksCount;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Series) && getId().equals(((Series) o).getId());
    }

}

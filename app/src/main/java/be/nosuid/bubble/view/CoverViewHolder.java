package be.nosuid.bubble.view;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import be.nosuid.bubble.R;

public abstract class CoverViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView mCoverView;
    private TextView mTitleTextView;
    private TextView mProgressTextView;

    public CoverViewHolder(View itemView) {
        super(itemView);
        mCoverView = itemView.findViewById(R.id.coverImageView);
        mTitleTextView = itemView.findViewById(R.id.coverTitleTextView);
        mProgressTextView = itemView.findViewById(R.id.coverProgressTextView);

        itemView.setClickable(true);
        itemView.setOnClickListener(this);
    }

    protected void setupText(String title, int progress, int maxProgress) {
        mTitleTextView.setText(title);
        mProgressTextView.setText(String.format("%d/%d", progress, maxProgress));
    }

    protected void setupCover(Picasso picasso, Uri thumbnail, Object tag) {
        picasso.load(thumbnail)
                .tag(tag)
                .into(mCoverView);
    }

    @Override
    public abstract void onClick(View v);
}

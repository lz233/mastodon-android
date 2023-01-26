package org.joinmastodon.android.ui.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import org.joinmastodon.android.R;
import org.joinmastodon.android.fragments.BaseStatusListFragment;
import org.joinmastodon.android.fragments.NotificationsListFragment;
import org.joinmastodon.android.ui.PhotoLayoutHelper;
import org.joinmastodon.android.ui.displayitems.ImageStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.LinkCardStatusDisplayItem;
import org.joinmastodon.android.ui.displayitems.StatusDisplayItem;

import java.util.List;
import java.util.logging.Logger;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.utils.V;

public class InsetStatusItemDecoration extends RecyclerView.ItemDecoration{
	private final BaseStatusListFragment<?> listFragment;
	private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
	private int bgColor;
	private int borderColor;
	private RectF rect=new RectF();

	public InsetStatusItemDecoration(BaseStatusListFragment<?> listFragment){
		this.listFragment=listFragment;
		bgColor=UiUtils.getThemeColor(listFragment.getActivity(), android.R.attr.colorBackground);
		borderColor=UiUtils.getThemeColor(listFragment.getActivity(), R.attr.colorPollVoted);
	}

	@Override
	public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
		List<StatusDisplayItem> displayItems=listFragment.getDisplayItems();
		int pos=0;
		for(int i=0; i<parent.getChildCount(); i++){
			View child=parent.getChildAt(i);
			RecyclerView.ViewHolder holder=parent.getChildViewHolder(child);
			pos=holder.getAbsoluteAdapterPosition();
			boolean inset=(holder instanceof StatusDisplayItem.Holder<?> sdi) && sdi.getItem().inset;
			if(inset){
				if(rect.isEmpty()){
					rect.set(child.getX(), i==0 && pos>0 && displayItems.get(pos-1).inset ? V.dp(-10) : child.getY(), child.getX()+child.getWidth(), child.getY()+child.getHeight());
				}else{
					if(holder instanceof ImageStatusDisplayItem.Holder<?>){
						rect.bottom=Math.max(rect.bottom, child.getY()+child.getHeight()+V.dp(16));
					}else {
						rect.bottom=Math.max(rect.bottom, child.getY()+child.getHeight());
					}
				}
			}else if(!rect.isEmpty()){
				drawInsetBackground(parent, c);
				rect.setEmpty();
			}
		}
		if(!rect.isEmpty()){
			if(pos<displayItems.size()-1 && displayItems.get(pos+1).inset){
				rect.bottom=parent.getHeight()+V.dp(10);
			}
			drawInsetBackground(parent, c);
			rect.setEmpty();
		}
	}

	private void drawInsetBackground(RecyclerView list, Canvas c){
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(bgColor);
		rect.left=V.dp(12);
		rect.right=list.getWidth()-V.dp(12);
		rect.inset(V.dp(4), V.dp(4));
		c.drawRoundRect(rect, V.dp(4), V.dp(4), paint);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(V.dp(1));
		paint.setColor(borderColor);
		rect.inset(paint.getStrokeWidth()/2f, paint.getStrokeWidth()/2f);
		c.drawRoundRect(rect, V.dp(4), V.dp(4), paint);
	}

	@Override
	public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
		List<StatusDisplayItem> displayItems=listFragment.getDisplayItems();
		RecyclerView.ViewHolder holder=parent.getChildViewHolder(view);
		if(holder instanceof StatusDisplayItem.Holder<?> sdi){
			boolean inset=sdi.getItem().inset;
			int pos=holder.getAbsoluteAdapterPosition();
			if(inset){
				boolean topSiblingInset=pos>0 && displayItems.get(pos-1).inset;
				boolean bottomSiblingInset=pos<displayItems.size()-1 && displayItems.get(pos+1).inset;
				int pad;
				if(holder instanceof ImageStatusDisplayItem.Holder || holder instanceof LinkCardStatusDisplayItem.Holder)
					pad=V.dp(16);
				else
					pad=V.dp(12);
				boolean insetLeft=true, insetRight=true;
				if(holder instanceof ImageStatusDisplayItem.Holder<?> img){
					PhotoLayoutHelper.TiledLayoutResult layout=img.getItem().tiledLayout;
					PhotoLayoutHelper.TiledLayoutResult.Tile tile=img.getItem().thisTile;
					// only inset those items that are on the edges of the layout
					insetLeft=tile.startCol==0;
					insetRight=tile.startCol+tile.colSpan==layout.columnSizes.length;
					// inset all items in the bottom row
					if(tile.startRow+tile.rowSpan==layout.rowSizes.length)
						bottomSiblingInset=false;
				}
				if(insetLeft)
					outRect.left=pad;
				if(insetRight)
					outRect.right=pad;
				if(!topSiblingInset)
					outRect.top=pad;
				if(holder instanceof ImageStatusDisplayItem.Holder<?>){
					if(!bottomSiblingInset)
						outRect.bottom=V.dp(32);
				}else {
					if(!bottomSiblingInset)
						outRect.bottom=pad;
				}
			}
		}
	}
}

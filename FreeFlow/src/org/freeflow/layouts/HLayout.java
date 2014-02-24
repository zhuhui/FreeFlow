package org.freeflow.layouts;

import java.util.HashMap;

import org.freeflow.core.SectionedAdapter;
import org.freeflow.core.ItemProxy;
import org.freeflow.core.Section;
import org.freeflow.layouts.AbstractLayout.FreeFlowLayoutParams;
import org.freeflow.layouts.VGridLayout.LayoutParams;
import org.freeflow.utils.ViewUtils;

import android.graphics.Rect;
import android.util.Log;

public class HLayout extends AbstractLayout {

	private boolean layoutChanged = false;
	private static final String TAG = "HLayout";
	private int itemWidth = -1;
	protected int width = -1;
	protected int height = -1;
	private SectionedAdapter itemsAdapter;
	private HashMap<Object, ItemProxy> proxies = new HashMap<Object, ItemProxy>();
	private int headerHeight = -1;
	private int headerWidth = -1;

	private int cellBufferSize = 0;
	private int bufferCount = 1;
	
	@Override
	public void setLayoutParams(FreeFlowLayoutParams params){
		
		if(params.equals(this.layoutParams)){
			return;
		}
		
		LayoutParams lp = (LayoutParams)params;
		this.itemWidth = lp.itemWidth;
		this.headerWidth = lp.headerWidth;
		this.headerHeight = lp.headerHeight;
		cellBufferSize = bufferCount * cellBufferSize;
		layoutChanged = true;
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDimensions(int measuredWidth, int measuredHeight) {
		if (measuredHeight == height && measuredWidth == width) {
			return;
		}
		this.width = measuredWidth;
		this.height = measuredHeight;
		layoutChanged = true;
	}

	@Override
	public void setAdapter(SectionedAdapter adapter) {
		if(adapter == this.itemsAdapter)
			return;
		this.itemsAdapter = adapter;
		layoutChanged = true;
	}

	public void generateItemProxies() {
		if (itemWidth < 0) {
			throw new IllegalStateException("itemWidth not set");
		}

		layoutChanged = false;

		proxies.clear();
		int leftStart = 0;

		for (int i = 0; i < itemsAdapter.getNumberOfSections(); i++) {
			Section s = itemsAdapter.getSection(i);

			if (itemsAdapter.shouldDisplaySectionHeaders()) {

				if (headerWidth < 0) {
					throw new IllegalStateException("headerWidth not set");
				}

				if (headerHeight < 0) {
					throw new IllegalStateException("headerHeight not set");
				}

				ItemProxy header = new ItemProxy();
				Rect hframe = new Rect();
				header.itemSection = i;
				header.itemIndex = -1;
				header.isHeader = true;
				hframe.left = leftStart;
				hframe.top = 0;
				hframe.right = leftStart + headerWidth;
				hframe.bottom = headerHeight;
				header.frame = hframe;
				header.data = s.getSectionTitle();
				proxies.put(header.data, header);

				leftStart += headerWidth;
			}

			for (int j = 0; j < s.getDataCount(); j++) {
				ItemProxy descriptor = new ItemProxy();
				Rect frame = new Rect();
				descriptor.itemSection = i;
				descriptor.itemIndex = j;
				frame.left = j * itemWidth + leftStart;
				frame.top = 0;
				frame.right = frame.left + itemWidth;
				frame.bottom = height;
				descriptor.frame = frame;
				descriptor.data = s.getDataAtIndex(j);
				proxies.put(descriptor.data, descriptor);
			}

			leftStart += s.getDataCount() * itemWidth;
		}

	}

	/**
	 * NOTE: In this instance, we subtract/add the cellBufferSize (computed when
	 * item width is set, defaulted to 1 cell) to add a buffer of cellBufferSize
	 * to each end of the viewport. <br>
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public HashMap<? extends Object, ItemProxy> getItemProxies(int viewPortLeft, int viewPortTop) {
		HashMap<Object, ItemProxy> desc = new HashMap<Object, ItemProxy>();

		if (proxies.size() == 0 || layoutChanged) {
			generateItemProxies();
		}

		for (ItemProxy fd : proxies.values()) {

			if (fd.frame.left + itemWidth > viewPortLeft - cellBufferSize
					&& fd.frame.left < viewPortLeft + width + cellBufferSize) {
				ItemProxy newDesc = ItemProxy.clone(fd);
				desc.put(newDesc.data, newDesc);
			}
		}

		return desc;
	}
	
	@Override
	public ItemProxy getItemAt(float x, float y){
		return ViewUtils.getItemAt(proxies, (int)x, (int)y);
	}

	@Override
	public boolean horizontalScrollEnabled() {
		return true;
	}

	@Override
	public boolean verticalScrollEnabled() {
		return false;
	}

	@Override
	public int getContentWidth() {
		if (itemsAdapter == null)
			return 0;

		int sectionIndex = itemsAdapter.getNumberOfSections() - 1;
		Section s = itemsAdapter.getSection(sectionIndex);

		if (s.getDataCount() == 0)
			return 0;

		Object lastFrameData = s.getDataAtIndex(s.getDataCount() - 1);
		ItemProxy fd = proxies.get(lastFrameData);

		return (fd.frame.left + fd.frame.width());
	}

	@Override
	public int getContentHeight() {
		return height;
	}

	@Override
	public ItemProxy getItemProxyForItem(Object data) {
		if (proxies.size() == 0 || layoutChanged) {
			generateItemProxies();
		}

		ItemProxy fd = ItemProxy.clone(proxies.get(data));
		return fd;
	}

	public void setBufferCount(int bufferCount) {
		this.bufferCount = bufferCount;
	}
	
	public static class LayoutParams extends FreeFlowLayoutParams{
		public int itemWidth = 0;
		public int headerWidth = 0;
		public int headerHeight = 0;
		
		public LayoutParams(int itemWidth){
			this.itemWidth = itemWidth;
		}
		
		public LayoutParams(int itemWidth, int headerWidth, int headerHeight){
			this.itemWidth = itemWidth;
			this.headerWidth = headerWidth;
			this.headerHeight = headerHeight;
		}
	}

}

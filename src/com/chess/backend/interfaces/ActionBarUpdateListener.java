package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import android.content.Context;
import com.chess.R;
import com.chess.ui.activities.CoreActivityActionBar;

/**
 * ActionBarUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ActionBarUpdateListener<ItemType> extends AbstractUpdateListener<ItemType> {
	private ActionBarHelper actionBarHelper;
	private CoreActivityActionBar coreActivityActionBar;


	public ActionBarUpdateListener(Context context, Class<ItemType> clazz) {
		super(context, clazz);
	}

	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar, Class<ItemType> clazz) {
		super(coreActivityActionBar, clazz);
		init(coreActivityActionBar);
	}

	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar) {
		super(coreActivityActionBar);
		init(coreActivityActionBar);
	}

	private void init(CoreActivityActionBar coreActivityActionBar){
		this.coreActivityActionBar = coreActivityActionBar;
		actionBarHelper = coreActivityActionBar.provideActionBarHelper();

	}

	@Override
	public void showProgress(boolean show) {
		if(actionBarHelper != null) {
			actionBarHelper.setRefreshActionItemState(show);
		}
	}

	@Override
	public void errorHandle(String resultMessage) {
		coreActivityActionBar.safeShowSinglePopupDialog(R.string.error, resultMessage);
	}
}

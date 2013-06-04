package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.ArticleItem;
import com.chess.backend.entity.new_api.CommonFeedCategoryItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveArticleCategoriesTask;
import com.chess.db.tasks.SaveArticlesListTask;
import com.chess.ui.adapters.CategoriesAdapter;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.NewArticlesThumbCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticlesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final int LATEST_ARTICLES_CNT = 3;

	private static final int LATEST_SECTION = 0;
	private static final int CATEGORIES_SECTION = 1;

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private NewArticlesThumbCursorAdapter articlesCursorAdapter;
	private CategoriesAdapter categoriesAdapter;

	private ArticleItemUpdateListener latestArticleUpdateListener;
	private SaveArticlesUpdateListener saveArticlesUpdateListener;
	private ArticlesCursorUpdateListener articlesCursorUpdateListener;

	private CategoriesUpdateListener categoriesUpdateListener;
	private SaveCategoriesUpdateListener saveCategoriesUpdateListener;

	private boolean need2Update = true;
	private CustomSectionedAdapter sectionedAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_light,
				new int[]{LATEST_SECTION, CATEGORIES_SECTION});

		articlesCursorAdapter = new NewArticlesThumbCursorAdapter(getActivity(), null);
		categoriesAdapter = new CategoriesAdapter(getActivity(), null);

		sectionedAdapter.addSection(getString(R.string.articles), articlesCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.new_my_move), categoriesAdapter); // TODO rename
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_articles_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		if (need2Update) {
			boolean haveSavedData = DBDataManager.haveSavedArticles(getActivity());

			loadCategoriesFromDB();
			if (AppUtils.isNetworkAvailable(getActivity())) {
				getCategories();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		} else {
			loadCategoriesFromDB();
			loadFromDb();
		}
	}

	private void loadCategoriesFromDB() {
		// show list of categories
		Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.ARTICLE_CATEGORIES], null, null, null, null);
		final List<String> list = new ArrayList<String>();
		if (!cursor.moveToFirst()) {
			return;
		}

		do {
			list.add(DBDataManager.getString(cursor, DBConstants.V_NAME));
		} while (cursor.moveToNext());

		categoriesAdapter.setItemsList(list);
		sectionedAdapter.notifyDataSetChanged();
		listView.setAdapter(sectionedAdapter);
	}

	private void init() {
		latestArticleUpdateListener = new ArticleItemUpdateListener();
		saveArticlesUpdateListener = new SaveArticlesUpdateListener();
		articlesCursorUpdateListener = new ArticlesCursorUpdateListener();

		categoriesUpdateListener = new CategoriesUpdateListener();
		saveCategoriesUpdateListener = new SaveCategoriesUpdateListener();
	}

	private void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_LIST);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, LATEST_ARTICLES_CNT);

		new RequestJsonTask<ArticleItem>(latestArticleUpdateListener).executeTask(loadItem);
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ARTICLES_CATEGORIES);
		new RequestJsonTask<CommonFeedCategoryItem>(categoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == LATEST_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			getActivityFace().openFragment(ArticleDetailsFragment.newInstance(DBDataManager.getId(cursor)));
		} else if (section == CATEGORIES_SECTION) {
			String sectionName= (String) adapterView.getItemAtPosition(position);

			getActivityFace().openFragment(ArticleCategoriesFragment.newInstance(sectionName));
		}
	}

	private class ArticleItemUpdateListener extends ChessUpdateListener<ArticleItem> {

		public ArticleItemUpdateListener() {
			super(ArticleItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem returnedObj) {
			new SaveArticlesListTask(saveArticlesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private class CategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public CategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			new SaveArticleCategoriesTask(saveCategoriesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private class SaveCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {
		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			// show list of categories
			loadCategoriesFromDB();

			// loading articles
			ArticlesFragment.this.updateData();
		}
	}

	private class SaveArticlesUpdateListener extends ChessUpdateListener<ArticleItem.Data> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(ArticleItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(articlesCursorUpdateListener, DbHelper.getArticlesListParams(LATEST_ARTICLES_CNT),
				getContentResolver()).executeTask();
	}

	private class ArticlesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			articlesCursorAdapter.changeCursor(returnedObj);
			sectionedAdapter.notifyDataSetChanged();

			need2Update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}


	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (sectionedAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);
			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
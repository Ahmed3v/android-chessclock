package com.chess.backend;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.FriendsItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.AppData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.06.13
 * Time: 22:44
 */
public class GetAndSaveFriends extends IntentService {

	protected static String[] arguments = new String[2];

	public GetAndSaveFriends() {
		super("GetAndSaveFriends");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AppData appData = new AppData(this);

		LoadItem loadItem = LoadHelper.getFriends(appData.getUserToken());

		FriendsItem item = null;
		try {
			item  = RestHelper.getInstance().requestData(loadItem, FriendsItem.class, getApplicationContext());
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			String username = appData.getUsername();
			ContentResolver contentResolver = getContentResolver();

			for (FriendsItem.Data currentItem : item.getData()) {
				final String[] arguments2 = arguments;
				arguments2[0] = String.valueOf(username);
				arguments2[1] = String.valueOf(currentItem.getUserId());

				// TODO implement beginTransaction logic for performance increase
				Uri uri = DbScheme.uriArray[DbScheme.Tables.FRIENDS.ordinal()];
				Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_USER_ID, DbDataManager.SELECTION_USER_ID, arguments2, null);

				ContentValues values = DbDataManager.putFriendItemToValues(currentItem, username);

				DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
			}
		}
	}
}

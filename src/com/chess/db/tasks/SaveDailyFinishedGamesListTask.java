package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.DailyFinishedGameData;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.util.List;


//public class SaveDailyFinishedGamesListTask extends AbstractUpdateTask<GameListFinishedItem, Long> {
//public class SaveDailyFinishedGamesListTask extends SaveDailyGamesTask<GameListFinishedItem> {
public class SaveDailyFinishedGamesListTask extends SaveDailyGamesTask<DailyFinishedGameData> {

	public SaveDailyFinishedGamesListTask(TaskUpdateInterface<DailyFinishedGameData> taskFace,
										  List<DailyFinishedGameData> finishedItems, ContentResolver resolver) {
        super(taskFace, finishedItems, resolver);
    }

	@Override
    protected Integer doTheTask(Long... ids) {
		Context context = getTaskFace().getMeContext();
		String userName = AppData.getUserName(context);

		for (DailyFinishedGameData finishedItem : itemList) {

			final String[] arguments2 = arguments;
			arguments2[0] = String.valueOf(userName);
			arguments2[1] = String.valueOf(finishedItem.getGameId()); // Test

			Uri uri = DBConstants.uriArray[DBConstants.ECHESS_FINISHED_LIST_GAMES];
			Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_GAME_ID,
					DBDataManager.SELECTION_GAME_ID, arguments2, null);

			ContentValues values = DBDataManager.putEchessFinishedListGameToValues(finishedItem, userName);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DBDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();

			updateOnlineGame(finishedItem.getGameId(), userName);
		}

        result = StaticData.RESULT_OK;

        return result;
    }

}

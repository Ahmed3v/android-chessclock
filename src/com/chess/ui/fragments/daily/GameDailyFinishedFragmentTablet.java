package com.chess.ui.fragments.daily;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 9:13
 */
public class GameDailyFinishedFragmentTablet extends GameDailyFinishedFragment {

	public GameDailyFinishedFragmentTablet() { }

	public static GameDailyFinishedFragmentTablet createInstance(long gameId, String username) {
		GameDailyFinishedFragmentTablet fragment = new GameDailyFinishedFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragmentTablet.createInstance(gameId, username));
	}
}

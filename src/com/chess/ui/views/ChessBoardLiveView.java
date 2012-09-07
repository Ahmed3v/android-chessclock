package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardLiveView extends ChessBoardNetworkView {

	public ChessBoardLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected boolean need2ShowSubmitButtons() {
		return preferences.getBoolean(AppData.getUserName(getContext()) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false);
	}

	@Override
	protected boolean isGameOver() {
		if (!boardFace.isPossibleToMakeMoves()) {
			if (boardFace.inCheck(boardFace.getSide())) {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();
			}
			finished = true; // todo: probably it is better to set Finished flag by lcc.onGameEnded event
			return true;
		}

		if (boardFace.inCheck(boardFace.getSide())) {
			boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
			gameActivityFace.invalidateGameScreen();
			gameActivityFace.onCheck();
		}
		return false;
	}

}

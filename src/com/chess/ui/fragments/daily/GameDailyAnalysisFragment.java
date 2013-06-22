package com.chess.ui.fragments.daily;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.new_api.DailyCurrentGameData;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.PopupItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.engine.MoveParser;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameAnalysisFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.chess_boards.ChessBoardAnalysisView;
import com.chess.ui.views.drawables.BoardAvatarDrawable;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.game_controls.ControlsAnalysisView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 7:26
 */
public class GameDailyAnalysisFragment extends GameBaseFragment implements GameAnalysisFace {

	public static final String DOUBLE_SPACE = "  ";
	private static final String ERROR_TAG = "send request failed popup";

	private static final int CURRENT_GAME = 0;

	private ChessBoardAnalysisView boardView;

	private DailyCurrentGameData currentGame;
	private long gameId;

	protected boolean userPlayWhite = true;
	private LoadFromDbUpdateListener loadFromDbUpdateListener;
	private NotationView notationsView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private ControlsAnalysisView controlsView;
	private ImageView topAvatarImg;
	private ImageView bottomAvatarImg;
	private BoardAvatarDrawable opponentAvatarDrawable;
	private BoardAvatarDrawable userAvatarDrawable;
	private LabelsConfig labelsConfig;

	public static GameDailyAnalysisFragment createInstance(long gameId) {
		GameDailyAnalysisFragment fragment = new GameDailyAnalysisFragment();
		fragment.gameId = gameId;
		Bundle arguments = new Bundle();
		arguments.putLong(BaseGameItem.GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		labelsConfig = new LabelsConfig();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_boardview_daily_analysis, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.analysis);

		widgetsInit(view);
	}

	private void widgetsInit(View view) {
		controlsView = (ControlsAnalysisView) view.findViewById(R.id.controlsAnalysisView);
		notationsView = (NotationView) view.findViewById(R.id.notationsView);
		topPanelView = (PanelInfoGameView) view.findViewById(R.id.topPanelView);
		bottomPanelView = (PanelInfoGameView) view.findViewById(R.id.bottomPanelView);

		{// set avatars
			Drawable src = new IconDrawable(getActivity(), R.string.ic_profile,
					R.color.new_normal_grey_2, R.dimen.board_avatar_icon_size);
			opponentAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);
			userAvatarDrawable = new BoardAvatarDrawable(getActivity(), src);

			topAvatarImg = (ImageView) topPanelView.findViewById(PanelInfoGameView.AVATAR_ID);
			bottomAvatarImg = (ImageView) bottomPanelView.findViewById(PanelInfoGameView.AVATAR_ID);

			labelsConfig.topAvatar = opponentAvatarDrawable;
			labelsConfig.bottomAvatar = userAvatarDrawable;
		}

		controlsView.enableGameControls(false);

		boardView = (ChessBoardAnalysisView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setTopPanelView(topPanelView);
		boardView.setBottomPanelView(bottomPanelView);
		boardView.setControlsView(controlsView);
		boardView.setNotationsView(notationsView);

		setBoardView(boardView);

		boardView.setGameActivityFace(this);
		boardView.lockBoard(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		DataHolder.getInstance().setInOnlineGame(gameId, true);
		loadGame();
	}

	public void init() {
		gameId = getArguments().getLong(BaseGameItem.GAME_ID, 0);

		loadFromDbUpdateListener = new LoadFromDbUpdateListener(CURRENT_GAME);

//		showActionRefresh = true;  // TODO restore
	}


	@Override
	public void onPause() {
		super.onPause();

		if (HONEYCOMB_PLUS_API) {
			dismissDialogs();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		DataHolder.getInstance().setInOnlineGame(gameId, false);
	}

	private void loadGame() {
		// load game from DB. After load update
		new LoadDataFromDbTask(loadFromDbUpdateListener, DbHelper.getDailyGameParams(getActivity(), gameId),
				getContentResolver()).executeTask();
	}

	@Override
	public void restart() {
		adjustBoardForGame();
	}

	@Override
	public void closeBoard() {
		getActivityFace().showPreviousFragment();
	}

	private class LoadFromDbUpdateListener extends AbstractUpdateListener<Cursor> {

		private int listenerCode;

		public LoadFromDbUpdateListener(int listenerCode) {
			super(getContext());
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			getSoundPlayer().playGameStart();

			currentGame = DBDataManager.getDailyCurrentGameFromCursor(returnedObj);
			returnedObj.close();

			userPlayWhite = currentGame.getWhiteUsername().equals(AppData.getUserName(getActivity()));

			labelsConfig.topAvatar = opponentAvatarDrawable;
			labelsConfig.bottomAvatar = userAvatarDrawable;

			if (userPlayWhite) {
				labelsConfig.userSide = ChessBoard.WHITE_SIDE;
				labelsConfig.topPlayerLabel = getBlackPlayerName();
				labelsConfig.bottomPlayerLabel = getWhitePlayerName();
			} else {
				labelsConfig.userSide = ChessBoard.BLACK_SIDE;
				labelsConfig.topPlayerLabel = getWhitePlayerName();
				labelsConfig.bottomPlayerLabel = getBlackPlayerName();
			}

			DataHolder.getInstance().setInOnlineGame(currentGame.getGameId(), true);

			controlsView.enableGameControls(true);
			boardView.lockBoard(false);

			adjustBoardForGame();

			getBoardFace().setJustInitialized(false);
		}
	}

	private void adjustBoardForGame() {
//		boardView.setFinished(false);
		getBoardFace().setFinished(false);

//		boardView.updatePlayerNames(getWhitePlayerName(), getBlackPlayerName()); // TODO recheck logic

//		timeRemains = gameInfoItem.getTimeRemaining() + gameInfoItem.getTimeRemainingUnits();

		if (isUserMove()) {
//			topPanelView.setTimeRemain(seconds);
		} else {
			// TODO set greyed timeLeft
//			topPanelView.setTimeRemain(seconds);
		}

		ChessBoardOnline.resetInstance();
		BoardFace boardFace = getBoardFace();
		if (currentGame.getGameType() == BaseGameItem.CHESS_960) {
			boardFace.setChess960(true);
		}

		if (!userPlayWhite) {
			boardFace.setReside(true);
		}

		String FEN = currentGame.getStartingFenPosition();
		if (!FEN.equals(StaticData.SYMBOL_EMPTY)) {
			boardFace.genCastlePos(FEN);
			MoveParser.fenParse(FEN, boardFace);
		}

		if (currentGame.getMoveList().contains(BaseGameItem.FIRST_MOVE_INDEX)) {
			String[] moves = currentGame.getMoveList()
					.replaceAll(AppConstants.MOVE_NUMBERS_PATTERN, StaticData.SYMBOL_EMPTY)
					.replaceAll(DOUBLE_SPACE, StaticData.SYMBOL_SPACE).substring(1).split(StaticData.SYMBOL_SPACE);   // Start after "+" sign

			boardFace.setMovesCount(moves.length);
			for (int i = 0, cnt = boardFace.getMovesCount(); i < cnt; i++) {
				boardFace.updateMoves(moves[i], false);
			}
		} else {
			boardFace.setMovesCount(0);
		}


		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

		boardFace.setJustInitialized(false);
	}


	@Override
	public void toggleSides() { // TODO
//		if (labelsConfig.userSide == ChessBoard.WHITE_SIDE) {
//			labelsConfig.userSide = ChessBoard.BLACK_SIDE;
//		} else {
//			labelsConfig.userSide = ChessBoard.WHITE_SIDE;
//		}
//		BoardAvatarDrawable tempDrawable = labelsConfig.topAvatar;
//		labelsConfig.topAvatar = labelsConfig.bottomAvatar;
//		labelsConfig.bottomAvatar = tempDrawable;
//
//		String tempLabel = labelsConfig.topPlayerName;
//		labelsConfig.topPlayerName = labelsConfig.bottomPlayerName;
//		labelsConfig.bottomPlayerName = tempLabel;
//
//		String tempScore = labelsConfig.topPlayerRating;
//		labelsConfig.topPlayerRating = labelsConfig.bottomPlayerRating;
//		labelsConfig.bottomPlayerRating = tempScore;
	}

	@Override
	public void invalidateGameScreen() {

		userAvatarDrawable.setSide(labelsConfig.userSide);
		opponentAvatarDrawable.setSide(labelsConfig.getOpponentSide());

		topAvatarImg.setImageDrawable(labelsConfig.topAvatar);
		bottomAvatarImg.setImageDrawable(labelsConfig.bottomAvatar);

		topPanelView.setSide(labelsConfig.getOpponentSide());
		bottomPanelView.setSide(labelsConfig.userSide);



//		whitePlayerLabel.setText(getWhitePlayerName());
//		blackPlayerLabel.setText(getBlackPlayerName());

		boardView.updateNotations(getBoardFace().getNotationArray());
	}

	@Override
	public String getWhitePlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getWhiteUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getWhiteRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public String getBlackPlayerName() {
		if (currentGame == null)
			return StaticData.SYMBOL_EMPTY;
		else
			return currentGame.getBlackUsername() + StaticData.SYMBOL_LEFT_PAR
					+ currentGame.getBlackRating() + StaticData.SYMBOL_RIGHT_PAR;
	}

	@Override
	public boolean currentGameExist() {
		return currentGame != null;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardOnline.getInstance(this);
	}

	@Override
	public void updateAfterMove() {
		if (currentGame == null) { // TODO fix inappropriate state, current game can't be null here // if we don't have Game entity
			// get game entity
			throw new IllegalStateException("Current game became NULL");
		}
	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {
	}

	@Override
	public Boolean isUserColorWhite() {
		if (currentGame != null)
			return currentGame.getWhiteUsername().equals(AppData.getUserName(getActivity()));
		else
			return null;
	}

	@Override
	public Long getGameId() {
		return gameId;
	}

	@Override
	public void showOptions(View view) {
	}

	private boolean isUserMove() {
		userPlayWhite = currentGame.getWhiteUsername().equals(AppData.getUserName(getActivity()));

		return /*(*/currentGame.isMyTurn()/*>WhiteMove() && userPlayWhite)
				|| (!currentGame.isWhiteMove() && !userPlayWhite)*/;
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(ERROR_TAG)) {
			backToLoginFragment();
		}
		super.onPositiveBtnClick(fragment);
	}

	protected void changeChatIcon(Menu menu) {
//		MenuItem menuItem = menu.findItem(R.id.menu_chat);
//		if (menuItem == null)
//			return;
//
//		if (currentGame.hasNewMessage()) {
//			menuItem.setIcon(R.drawable.chat_nm);
//		} else {
//			menuItem.setIcon(R.drawable.chat);
//		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) { // TODO restore, recheck
		if (currentGame != null) {
			changeChatIcon(menu);
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void showGameEndPopup(View layout, String message) {
		if (currentGame == null) {
			throw new IllegalStateException("showGameEndPopup starts with currentGame = null");
//			return;
		}

//		TextView endGameTitleTxt = (TextView) layout.findViewById(R.id.endGameTitleTxt);
		TextView endGameReasonTxt = (TextView) layout.findViewById(R.id.endGameReasonTxt);
		TextView yourRatingTxt = (TextView) layout.findViewById(R.id.yourRatingTxt);
//		endGameTitleTxt.setText(R.string.game_over); // already set to game over
		endGameReasonTxt.setText(message);

		int currentPlayerNewRating = getCurrentPlayerRating();

		String rating = getString(R.string.your_end_game_rating_online, currentPlayerNewRating);
		yourRatingTxt.setText(rating);

//		LinearLayout adViewWrapper = (LinearLayout) layout.findViewById(R.id.adview_wrapper);
//		MopubHelper.showRectangleAd(adViewWrapper, getActivity());
		PopupItem popupItem = new PopupItem();
		popupItem.setCustomView((LinearLayout) layout);

		PopupCustomViewFragment endPopupFragment = PopupCustomViewFragment.createInstance(popupItem);
		endPopupFragment.show(getFragmentManager(), END_GAME_TAG);

		layout.findViewById(R.id.newGamePopupBtn).setOnClickListener(this);
		layout.findViewById(R.id.rematchPopupBtn).setOnClickListener(this);
//		if (AppUtils.isNeedToUpgrade(getActivity())) {
//			layout.findViewById(R.id.upgradeBtn).setOnClickListener(this);
//		}
	}

	private int getCurrentPlayerRating() {
		if (userPlayWhite) {
			return currentGame.getWhiteRating();
		} else {
			return currentGame.getBlackRating();
		}
	}

	@Override
	protected void restoreGame() {
//		ChessBoardOnline.resetInstance();
		boardView.setGameActivityFace(this);

		adjustBoardForGame();
		getBoardFace().setJustInitialized(false);
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.newGamePopupBtn) {
			dismissDialogs();
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));

//			Intent intent = new Intent(this, OnlineNewGameActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
		}
	}


	private class LabelsConfig {
		int topPlayerSide;
		int bottomPlayerSide;
		String topPlayerLabel;
		String bottomPlayerLabel;
		Drawable topAvatar;
		Drawable bottomAvatar;
		int userSide;

		int getOpponentSide() {
			return userSide == ChessBoard.WHITE_SIDE ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;
		}
	}
}

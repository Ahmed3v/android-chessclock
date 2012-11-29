package com.chess.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardViewFace;
import com.chess.ui.interfaces.GameActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * ChessBoardBaseView class
 *
 * @author alien_roger
 * @created at: 25.04.12 10:54
 */
public abstract class ChessBoardBaseView extends ImageView implements BoardViewFace {

	public static final int P_ALPHA_ID = 0;
	public static final int P_BOOK_ID = 1;
	public static final int P_CASES_ID = 2;
	public static final int P_CLASSIC_ID = 3;
	public static final int P_CLUB_ID = 4;
	public static final int P_CONDAL_ID = 5;
	public static final int P_MAYA_ID = 6;
	public static final int P_MODERN_ID = 7;
	public static final int P_VINTAGE_ID = 8;


	protected Bitmap[][] piecesBitmaps;
	protected Bitmap boardBitmap;
	protected SharedPreferences preferences;

	protected boolean finished;
	protected boolean firstclick = true;
	protected boolean pieceSelected;
	protected boolean track;
	protected boolean drag;
	protected int[] pieces_tmp;
	protected int[] colors_tmp;

	protected int W;
	protected int H;
	protected int side;
	protected int square;
	protected int from = -1;
	protected int to = -1;
	protected int dragX = 0;
	protected int dragY = 0;
	protected int trackX = 0;
	protected int trackY = 0;

	protected Paint whitePaint;
	protected Paint coordinatesPaint;
	protected Paint redPaint;
	protected Paint greenPaint;

	protected String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	protected String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	protected int viewWidth = 0;
	protected int viewHeight = 0;

	protected float width;
	protected float height;
	protected Rect rect;

	protected GamePanelView gamePanelView;
	protected boolean isHighlightEnabled;
	protected boolean showCoordinates;
	protected String userName;
	protected boolean useTouchTimer;
	protected Handler handler;
	protected boolean userActive;
	protected Resources resources;
	protected GameActivityFace gameActivityFace;
	protected BoardFace boardFace;
	protected boolean locked;
	protected PaintFlagsDrawFilter drawFilter;
	private float density;

	public ChessBoardBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		drawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);

		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		handler = new Handler();
		greenPaint = new Paint();
		whitePaint = new Paint();
		coordinatesPaint = new Paint();
		redPaint = new Paint();
		rect = new Rect();

		whitePaint.setStrokeWidth(2.0f);
		whitePaint.setStyle(Style.STROKE);
		whitePaint.setColor(Color.WHITE);

		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		int coordinateFont = getResources().getInteger(R.integer.board_highlight_font);

		coordinatesPaint.setStrokeWidth(1.0f);
		coordinatesPaint.setStyle(Style.FILL);
		coordinatesPaint.setColor(Color.WHITE);
		coordinatesPaint.setShadowLayer(1.5f, 1.0f, 1.5f, 0xFF000000);
		coordinatesPaint.setTextSize(coordinateFont * density);
		coordinatesPaint.setTypeface(typeface);

		redPaint.setStrokeWidth(2.0f);
		redPaint.setStyle(Style.STROKE);
		redPaint.setColor(Color.RED);

		greenPaint.setStrokeWidth(2.0f);
		greenPaint.setStyle(Style.STROKE);
		greenPaint.setColor(Color.GREEN);


		width = resources.getDisplayMetrics().widthPixels;
		height = resources.getDisplayMetrics().heightPixels;

		handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
		userActive = false;

		preferences = AppData.getPreferences(getContext());

	}

	public void setGameActivityFace(GameActivityFace gameActivityFace) {
		this.gameActivityFace = gameActivityFace;
		userName = AppData.getUserName(getContext());

		isHighlightEnabled = AppData.isHighlightEnabled(getContext());

		showCoordinates = AppData.showCoordinates(getContext());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setMeasuredDimension(resolveSize((int) width, widthMeasureSpec),
					resolveSize((int) width, heightMeasureSpec));
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setMeasuredDimension(resolveSize((int) height, widthMeasureSpec),
					resolveSize((int) height, heightMeasureSpec));
		}
	}


	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(BoardFace boardFace) {
		this.boardFace = boardFace;
		onBoardFaceSet(boardFace);
	}

	protected void onBoardFaceSet(BoardFace boardFace) {
	}

	public void setGamePanelView(GamePanelView gamePanelView) {
		this.gamePanelView = gamePanelView;
		this.gamePanelView.setBoardViewFace(this);
	}

	@Override
	public void showOptions() {
		gameActivityFace.showOptions();
	}

	@Override
	public void flipBoard() {
		getBoardFace().setReside(!getBoardFace().isReside());

		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	@Override
	public void moveBack() {
		finished = false;
		pieceSelected = false;
		getBoardFace().takeBack();
		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	@Override
	public void switchAnalysis() {
		boolean isAnalysis = getBoardFace().toggleAnalysis();

		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, isAnalysis);
		gameActivityFace.switch2Analysis(isAnalysis);
	}

	public void enableAnalysis() {
		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, true);
		gamePanelView.enableAnalysisMode(true);
		gameActivityFace.switch2Analysis(true);
	}

//	@Override
//	public boolean isInAnalysis() {
//		return getBoardFace().isAnalysis();
//	}

	@Override
	public void moveForward() {
		pieceSelected = false;
		getBoardFace().takeNext();
		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public void newGame() {
		gameActivityFace.newGame();
	}

	public void setMovesLog(CharSequence move) {
		gamePanelView.setMovesLog(move);
		invalidate();
		requestFocus();
	}

	public void enableTouchTimer() {
		useTouchTimer = true;
	}

	protected Runnable checkUserIsActive = new Runnable() {
		@Override
		public void run() {
			if (userActive) {
				userActive = false;
				handler.removeCallbacks(this);
				handler.postDelayed(this, StaticData.WAKE_SCREEN_TIMEOUT);
			} else
				gameActivityFace.turnScreenOff();

		}
	};

	protected void drawBoard(Canvas canvas) {
		W = viewWidth;
		H = viewHeight;

		if (H < W) {
			square = viewHeight / 8;
			H -= viewHeight % 8;
		} else {
			square = viewWidth / 8;
			W -= viewWidth % 8;
		}

		side = square * 2;

		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				rect.set(i * side, j * side, i * side + side, j * side + side);
				canvas.drawBitmap(boardBitmap, null, rect, null);
			}
		}
	}

	protected void drawPieces(Canvas canvas) {
		int i;
		for (i = 0; i < 64; i++) {
			if (drag && i == from)
				continue;
			int color = boardFace.getColor()[i];
			int piece = boardFace.getPieces()[i];
			int x = ChessBoard.getColumn(i, boardFace.isReside());
			int y = ChessBoard.getRow(i, boardFace.isReside());
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {    // here is the simple replace/redraw of piece
				rect.set(x * square, y * square, x * square + square, y * square + square);
				canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
			}
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		int i;
		if (showCoordinates) {
			float numYShift = 15 * density;
			float textYShift = 3 * density;
			for (i = 0; i < 8; i++) {
				if (boardFace.isReside()) {
					canvas.drawText(nums[i], 2, i * square + numYShift, coordinatesPaint);
					canvas.drawText(signs[7 - i], i * square + textYShift, 8 * square - textYShift, coordinatesPaint);
				} else {
					canvas.drawText(nums[7 - i], 2, i * square + numYShift, coordinatesPaint);
					canvas.drawText(signs[i], i * square + textYShift, 8 * square - textYShift, coordinatesPaint);
				}
			}
		}
	}

	protected void drawHighlight(Canvas canvas) {
		if (isHighlightEnabled && boardFace.getHply() > 0) { // draw moved piece highlight from -> to
			Move move = boardFace.getHistDat()[boardFace.getHply() - 1].move;
			int x1 = ChessBoard.getColumn(move.from, boardFace.isReside());
			int y1 = ChessBoard.getRow(move.from, boardFace.isReside());
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, redPaint);
			int x2 = ChessBoard.getColumn(move.to, boardFace.isReside());
			int y2 = ChessBoard.getRow(move.to, boardFace.isReside());
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, redPaint);
		}

		if (pieceSelected) { // draw rectangle around the start move piece position
			int x = ChessBoard.getColumn(from, boardFace.isReside());
			int y = ChessBoard.getRow(from, boardFace.isReside());
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, whitePaint);
		}
	}

	protected void drawDragPosition(Canvas canvas) {
		if (drag) {
			int c = boardFace.getColor()[from];
			int p = boardFace.getPieces()[from];
			int halfSquare = square / 2;
			int x = dragX - halfSquare;
			int y = dragY - halfSquare;
			int col = (dragX - dragX % square) / square;
			int row = ((dragY + square) - (dragY + square) % square) / square;
			if (c != ChessBoard.EMPTY && p != ChessBoard.EMPTY) {
				rect.set(x - halfSquare, y - halfSquare, x + square + halfSquare, y + square + halfSquare);
				canvas.drawBitmap(piecesBitmaps[c][p], null, rect, null);
				canvas.drawRect(col * square - halfSquare, row * square - halfSquare,
						col * square + square + halfSquare, row * square + square + halfSquare, whitePaint);
			}
		}
	}

	protected void drawTrackballDrag(Canvas canvas) {
		if (track) {
			int x = (trackX - trackX % square) / square;
			int y = (trackY - trackY % square) / square;
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, greenPaint);
		}
	}

	protected void drawCapturedPieces() {
		// Count captured piecesBitmap
		gamePanelView.dropAlivePieces();

		for (int i = 0; i < 64; i++) {
			int pieceId = boardFace.getPiece(i);
			if (boardFace.getColor()[i] == ChessBoard.LIGHT) {
				gamePanelView.addAlivePiece(true, pieceId);
			} else {
				gamePanelView.addAlivePiece(false, pieceId);
			}
		}
		gamePanelView.updateCapturedPieces();
	}

//	protected void loadIdsFromResources(){  // TODO reuse later
//		TypedArray ar = getResources().obtainTypedArray(R.array.comp_strength);
//
//		int len = ar.length();
//		int[] resIds = new int[len];
//		for (int i = 0; i < len; i++)
//			resIds[i] = ar.getResourceId(i, 0);
//
//		ar.recycle();
//
//		// Do stuff with resolved reference array, resIds[]...
//		for (int i = 0; i < len; i++)
//			Log.v("TEST", "Res Id " + i + " is " + Integer.toHexString(resIds[i]));
//	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				return onActionDown(event);
			}
			case MotionEvent.ACTION_MOVE: {
				return onActionMove(event);
			}
			case MotionEvent.ACTION_UP: {
				return onActionUp(event);
			}
		}
		return super.onTouchEvent(event);    //To change body of overridden methods use File | Settings | File Templates.
	}

	protected boolean onActionDown(MotionEvent event) {
		int col = (int) (event.getX() - event.getX() % square) / square;
		int row = (int) (event.getY() - event.getY() % square) / square;
		if (col > 7 || col < 0 || row > 7 || row < 0) {
			invalidate();
			return false;
		}
		if (firstclick) {
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[from] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[from]) {
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		} else {
			int fromPosIndex = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[fromPosIndex] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[fromPosIndex]) {
				from = fromPosIndex;
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		}
		return true;
	}

	protected boolean onActionMove(MotionEvent event){
		dragX = (int) event.getX();
		dragY = (int) event.getY() - square;
		int col = (dragX - dragX % square) / square;
		int row = (dragY - dragY % square) / square;
		if (col > 7 || col < 0 || row > 7 || row < 0) {
			invalidate();
			return false;
		}
		if (!drag && !pieceSelected)
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
		if (!firstclick && boardFace.getSide() == boardFace.getColor()[from]) {
			drag = true;
			to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			invalidate();
		}
		return true;
	}

	protected boolean onActionUp(MotionEvent event) {
		int col = (int) (event.getX() - event.getX() % square) / square;
		int row = (int) (event.getY() - event.getY() % square) / square;

		drag = false;
		// if outside of the boardBitmap - return
		if (col > 7 || col < 0 || row > 7 || row < 0) { // if touched out of board
			invalidate();
			return false;
		}
		if (firstclick) {
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[from] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[from]) {
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		} else {
			to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			pieceSelected = false;
			firstclick = true;
			boolean found = false;
			TreeSet<Move> moves = boardFace.gen();
			Iterator<Move> moveIterator = moves.iterator();

			Move move = null;
			while (moveIterator.hasNext()) {
				move = moveIterator.next();     // search for move that was made
				if (move.from == from && move.to == to) {
					found = true;
					break;
				}
			}

			if ((((to < 8) && (boardFace.getSide() == ChessBoard.LIGHT)) ||
					((to > 55) && (boardFace.getSide() == ChessBoard.DARK))) &&
					(boardFace.getPieces()[from] == ChessBoard.PAWN) && found) {

				gameActivityFace.showChoosePieceDialog(col, row);
				return true;
			}

			if (found && boardFace.makeMove(move)) {
				afterMove();
			} else if (boardFace.getPieces()[to] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[to]) {
				pieceSelected = true;
				firstclick = false;
				from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			}
			invalidate();
		}
		return true;
	}


	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
	}

	public void promote(int promote, int col, int row) {
		boolean found = false;
		TreeSet<Move> moves = boardFace.gen();
		Iterator<Move> iterator = moves.iterator();

		Move move = null;
		while (iterator.hasNext()) {
			move = iterator.next();
			if (move.from == from && move.to == to && move.promote == promote) {
				found = true;
				break;
			}
		}
		if (found && boardFace.makeMove(move)) {
			invalidate();
			afterMove();
		} else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
			pieceSelected = true;
			firstclick = false;
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			invalidate();
		} else {
			invalidate();
		}
	}

	protected boolean isGameOver() {

		String message = null;
		if (!boardFace.isAnalysis()) {
			if (!boardFace.isPossibleToMakeMoves()) {
				if (boardFace.inCheck(boardFace.getSide())) {
					if (boardFace.getSide() == ChessBoard.LIGHT)
						message = getResources().getString(R.string.black_wins);
					else
						message = getResources().getString(R.string.white_wins);
				} else
					message = getResources().getString(R.string.draw_by_stalemate);
			} else if (boardFace.reps() == 3)
				message = getResources().getString(R.string.draw_by_3fold_repetition);
		} else if (boardFace.inCheck(boardFace.getSide())) {

			if (!boardFace.isPossibleToMakeMoves()) {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();
				finished = true;
				return true;
			} else {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
				gameActivityFace.invalidateGameScreen();
				gameActivityFace.onCheck();
			}
		}

		if (message != null) {
			finished = true;
			gameActivityFace.onGameOver(message, false);
			return true;
		}

		return false;
	}

	protected abstract void afterMove();

	private int[] boardsDrawables = {
			R.drawable.wood_dark,
			R.drawable.wood_light,
			R.drawable.blue,
			R.drawable.brown,
			R.drawable.green,
			R.drawable.grey,
			R.drawable.marble,
			R.drawable.red,
			R.drawable.tan
	};

	public void lockBoard(boolean lock) {
		locked = lock;
		gamePanelView.lock(lock);
		setEnabled(!lock);
	}

	protected void loadBoard(int boardId) {
		boardBitmap = ((BitmapDrawable) resources.getDrawable(boardsDrawables[boardId])).getBitmap();
	}

	private void setPieceBitmapFromArray(int[] drawableArray) {
		piecesBitmaps = new Bitmap[2][6];
		Resources resources = getResources();
		for (int j = 0; j < 6; j++) {
			piecesBitmaps[0][j] = ((BitmapDrawable) resources.getDrawable(drawableArray[j])).getBitmap();
		}
		for (int j = 0; j < 6; j++) {
			piecesBitmaps[1][j] = ((BitmapDrawable) resources.getDrawable(drawableArray[6 + j])).getBitmap();
		}
	}

	protected void loadPieces(int piecesSetId) {
		switch (piecesSetId) {
			case P_ALPHA_ID:
				setPieceBitmapFromArray(alphaPiecesDrawableIds);
				break;
			case P_BOOK_ID:
				setPieceBitmapFromArray(bookPiecesDrawableIds);
				break;
			case P_CASES_ID:
				setPieceBitmapFromArray(casesPiecesDrawableIds);
				break;
			case P_CLASSIC_ID:
				setPieceBitmapFromArray(classicPiecesDrawableIds);
				break;
			case P_CLUB_ID:
				setPieceBitmapFromArray(clubPiecesDrawableIds);
				break;
			case P_CONDAL_ID:
				setPieceBitmapFromArray(condalPiecesDrawableIds);
				break;
			case P_MAYA_ID:
				setPieceBitmapFromArray(mayaPiecesDrawableIds);
				break;
			case P_MODERN_ID:
				setPieceBitmapFromArray(modernPiecesDrawableIds);
				break;
			case P_VINTAGE_ID:
				setPieceBitmapFromArray(vintagePiecesDrawableIds);
				break;
		}
	}

	private int[] alphaPiecesDrawableIds = new int[]{
			R.drawable.alpha_wp,
			R.drawable.alpha_wn,
			R.drawable.alpha_wb,
			R.drawable.alpha_wr,
			R.drawable.alpha_wq,
			R.drawable.alpha_wk,
			R.drawable.alpha_bp,
			R.drawable.alpha_bn,
			R.drawable.alpha_bb,
			R.drawable.alpha_br,
			R.drawable.alpha_bq,
			R.drawable.alpha_bk,
	};

	private int[] bookPiecesDrawableIds = new int[]{
			R.drawable.book_wp,
			R.drawable.book_wn,
			R.drawable.book_wb,
			R.drawable.book_wr,
			R.drawable.book_wq,
			R.drawable.book_wk,
			R.drawable.book_bp,
			R.drawable.book_bn,
			R.drawable.book_bb,
			R.drawable.book_br,
			R.drawable.book_bq,
			R.drawable.book_bk,
	};

	private int[] casesPiecesDrawableIds = new int[]{
			R.drawable.cases_wp,
			R.drawable.cases_wn,
			R.drawable.cases_wb,
			R.drawable.cases_wr,
			R.drawable.cases_wq,
			R.drawable.cases_wk,
			R.drawable.cases_bp,
			R.drawable.cases_bn,
			R.drawable.cases_bb,
			R.drawable.cases_br,
			R.drawable.cases_bq,
			R.drawable.cases_bk,
	};

	private int[] classicPiecesDrawableIds = new int[]{
			R.drawable.classic_wp,
			R.drawable.classic_wn,
			R.drawable.classic_wb,
			R.drawable.classic_wr,
			R.drawable.classic_wq,
			R.drawable.classic_wk,
			R.drawable.classic_bp,
			R.drawable.classic_bn,
			R.drawable.classic_bb,
			R.drawable.classic_br,
			R.drawable.classic_bq,
			R.drawable.classic_bk,
	};

	private int[] clubPiecesDrawableIds = new int[]{
			R.drawable.club_wp,
			R.drawable.club_wn,
			R.drawable.club_wb,
			R.drawable.club_wr,
			R.drawable.club_wq,
			R.drawable.club_wk,
			R.drawable.club_bp,
			R.drawable.club_bn,
			R.drawable.club_bb,
			R.drawable.club_br,
			R.drawable.club_bq,
			R.drawable.club_bk,
	};


	private int[] condalPiecesDrawableIds = new int[]{
			R.drawable.condal_wp,
			R.drawable.condal_wn,
			R.drawable.condal_wb,
			R.drawable.condal_wr,
			R.drawable.condal_wq,
			R.drawable.condal_wk,
			R.drawable.condal_bp,
			R.drawable.condal_bn,
			R.drawable.condal_bb,
			R.drawable.condal_br,
			R.drawable.condal_bq,
			R.drawable.condal_bk,
	};

	private int[] mayaPiecesDrawableIds = new int[]{
			R.drawable.maya_wp,
			R.drawable.maya_wn,
			R.drawable.maya_wb,
			R.drawable.maya_wr,
			R.drawable.maya_wq,
			R.drawable.maya_wk,
			R.drawable.maya_bp,
			R.drawable.maya_bn,
			R.drawable.maya_bb,
			R.drawable.maya_br,
			R.drawable.maya_bq,
			R.drawable.maya_bk,
	};

	private int[] modernPiecesDrawableIds = new int[]{
			R.drawable.modern_wp,
			R.drawable.modern_wn,
			R.drawable.modern_wb,
			R.drawable.modern_wr,
			R.drawable.modern_wq,
			R.drawable.modern_wk,
			R.drawable.modern_bp,
			R.drawable.modern_bn,
			R.drawable.modern_bb,
			R.drawable.modern_br,
			R.drawable.modern_bq,
			R.drawable.modern_bk,
	};

	private int[] vintagePiecesDrawableIds = new int[]{
			R.drawable.vintage_wp,
			R.drawable.vintage_wn,
			R.drawable.vintage_wb,
			R.drawable.vintage_wr,
			R.drawable.vintage_wq,
			R.drawable.vintage_wk,
			R.drawable.vintage_bp,
			R.drawable.vintage_bn,
			R.drawable.vintage_bb,
			R.drawable.vintage_br,
			R.drawable.vintage_bq,
			R.drawable.vintage_bk,
	};

	public void updateBoardAndPiecesImgs() {
		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		invalidate();
	}
}

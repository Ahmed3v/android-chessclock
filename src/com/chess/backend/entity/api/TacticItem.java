package com.chess.backend.entity.api;


import com.chess.backend.statics.Symbol;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.12.12
 * Time: 11:23
 */
public class TacticItem extends BaseResponseItem<List<TacticItem.Data>> {
	/*
		"status": "success",
		"count": 3,
		"data": [
			{
				"id": 823,
				"initial_fen": "5r1k/1b4pp/8/p1R5/Pp2p2q/1Q2B2P/1P3PrK/4R3 w - - 1 2",
				"clean_move_string": "1. Kxg2 Rxf2+ 2. Bxf2 e3+ 3. Rd5 Qxf2+ 4. Kh1 Qxe1+ 5. Kh2 Qf2+ 6. Kh1 e2 ",
				"attempt_count": 1,
				"passed_count": 0,
				"rating": 1283,
				"average_seconds": 30
			}...
		]

        "tactics_problem": {
            "id": 873,
            "initial_fen": "6K1/3r3r/5kn1/5p1N/5P2/8/8/4R1R1 b KQkq - 1 1",
            "clean_move_string": "1... Rxh5 2. Rxg6+ Kxg6 3. Re6# ",
            "attempt_count": 2,
            "passed_count": 0,
            "rating": 1424,
            "average_seconds": 20
        }
	*/

	public static class Data {

		private long id;
		private String initial_fen;
		private String clean_move_string;
		private int attempt_count;
		private int passed_count;
		private int rating;
		private int average_seconds;
		/*Local addition */
		private String user;
		private long secondsSpent;
		private TacticRatingData resultItem;
		private boolean stop;
		private boolean answerWasShowed;
		private boolean retry;

		public void setId(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public String getInitialFen() {
			return initial_fen;
		}

		public void setMoveList(String moveList) {
			clean_move_string = moveList;
		}

		public String getCleanMoveString() {
			return clean_move_string;
		}

		public int getAttemptCnt() {
			return attempt_count;
		}

		public int getPassedCnt() {
			return passed_count;
		}

		public int getRating() {
			return rating;
		}

		public int getAvgSeconds() {
			return average_seconds;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public boolean isStop() {
			return stop;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		public boolean isAnswerWasShowed() {
			return answerWasShowed;
		}

		public void setAnswerWasShowed(boolean wasShowed) {
			this.answerWasShowed = wasShowed;
		}

		public boolean isRetry() {
			return retry;
		}

		public void setRetry(boolean retry) {
			this.retry = retry;
		}


		public String getSecondsSpentStr() {
			return AppUtils.getSecondsTimeFromSecondsStr(secondsSpent);
		}

		public void setSecondsSpent(long secondsSpent) {
			this.secondsSpent = secondsSpent;
		}

		public void increaseSecondsPassed() {
			secondsSpent++;
		}

		public TacticRatingData getResultItem() {
			return resultItem;
		}

		public void setResultItem(TacticRatingData resultItem) {
			this.resultItem = resultItem;
		}

		public long getSecondsSpent() {
			return secondsSpent;
		}




		public void setFen(String fen) {
			initial_fen = fen;
		}



		public void setAttemptCnt(int attemptCnt) {
			this.attempt_count = attemptCnt;
		}

		public void setPassedCnt(int passedCnt) {
			passed_count = passedCnt;
		}

		public void setRating(int rating) {
			this.rating = rating;
		}

		public void setAvgSeconds(int avgSeconds) {
			average_seconds = avgSeconds;
		}

		public String getPositiveScore() {
			int userRatingChangeInt = resultItem.getUserRatingChange();
			String userRatingChange = String.valueOf(userRatingChangeInt);
			String plusSymbol = (userRatingChangeInt > 0) ? Symbol.PLUS : Symbol.EMPTY;
			return Symbol.wrapInPars(plusSymbol + userRatingChange);
		}

		public String getNegativeScore() {
			int userRatingChangeInt = resultItem.getUserRatingChange();
			String userRatingChange = String.valueOf(userRatingChangeInt);
			return Symbol.wrapInPars(userRatingChange);
		}
	}

}

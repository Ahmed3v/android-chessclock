package com.chess.backend.entity.api;

import com.chess.statics.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.12.12
 * Time: 6:31
 */
public class DailyGameBaseData {
/*
	  id: 35002080,
	i_play_as: 1,
	game_type_id: 1,
	time_remaining: 2386489,
	fen: "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 1 2",
	timestamp: 1385535830,
	name: "Let's Play!",
	last_move_from_square: "e7",
	last_move_to_square: "e5",
	is_draw_offer_pending: false,
	is_opponent_online: true,
	is_my_turn: true,
	has_new_message: false,
	white_username: "anotherroger",
	black_username: "alien_roger",
	white_rating: 1425,
	black_rating: 1247,
	is_rated: false,
	days_per_move: 1,
	draw_offered: 0,
	encoded_moves_piotr_string: "mC0K",
	starting_fen_position: null,
	move_list: "1. e4 e5 ",
	white_avatar: "//s3.amazonaws.com/chess-4/images_users/avatars/anotherroger_origin.2.jpeg",
	black_avatar: "//s3.amazonaws.com/chess-4/images_users/avatars/alien_roger_origin.6.jpeg",
	white_premium_status: 2,
	black_premium_status: 3,
	white_country_id: 2,
	black_country_id: 116,
	white_first_name: "",
	white_last_name: "",
	black_first_name: "Alien",
	black_last_name: "Roger",
	is_tournament_game: false
*/

	private long id;
	private int i_play_as;
	private int user_to_move;
	private int game_type_id;
	private String fen;
	private long timestamp;
	private String name;
	private String last_move_from_square;
	private String last_move_to_square;
	private boolean is_opponent_online;
	private boolean has_new_message;
	private String white_username;
	private String black_username;
	private int white_rating;
	private int black_rating;
	private long time_remaining;
	private String starting_fen_position;
	private String move_list;
	private String white_avatar;
	private String black_avatar;
	private int black_country_id;
	private int white_country_id;
	private int white_premium_status;
	private int black_premium_status;
	private boolean is_rated;
	private int days_per_move;

	public long getGameId() {
		return id;
	}

	public void setGameId(long id) {
		this.id = id;
	}

	public int getMyColor() {
		return i_play_as;
	}

	public void setMyColor(int i_play_as) {
		this.i_play_as = i_play_as;
	}

	public int getUserToMove() {
		return user_to_move;
	}

	public void setUserToMove(int user_to_move) {
		this.user_to_move = user_to_move;
	}

	public int getGameType() {
		return game_type_id;
	}

	public void setGameType(int game_type) {
		this.game_type_id = game_type;
	}

	public long getTimeRemaining() {
		return time_remaining;
	}

	public void setTimeRemaining(long time_remaining) {
		this.time_remaining = time_remaining;
	}

	public String getFen() {
		return getSafeValue(fen);
	}

	public void setFen(String fen) {
		this.fen = fen;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLastMoveFromSquare() {
		return getSafeValue(last_move_from_square);
	}

	public void setLastMoveFromSquare(String last_move_from_square) {
		this.last_move_from_square = last_move_from_square;
	}

	public String getLastMoveToSquare() {
		return getSafeValue(last_move_to_square);
	}

	public void setLastMoveToSquare(String last_move_to_square) {
		this.last_move_to_square = last_move_to_square;
	}

	public boolean isOpponentOnline() {
		return is_opponent_online;
	}

	public void setOpponentOnline(boolean is_opponent_online) {
		this.is_opponent_online = is_opponent_online;
	}

	public String getWhiteUsername() {
		return getSafeValue(white_username);
	}

	public void setWhiteUsername(String white_username) {
		this.white_username = white_username;
	}

	public String getBlackUsername() {
		return getSafeValue(black_username);
	}

	public void setBlackUsername(String black_username) {
		this.black_username = black_username;
	}

	public int getWhiteRating() {
		return white_rating;
	}

	public void setWhiteRating(int white_rating) {
		this.white_rating = white_rating;
	}

	public int getBlackRating() {
		return black_rating;
	}

	public void setBlackRating(int black_rating) {
		this.black_rating = black_rating;
	}

	public String getWhiteAvatar() {
		return white_avatar;
	}

	public void setWhiteAvatar(String white_avatar) {
		this.white_avatar = white_avatar;
	}

	public String getBlackAvatar() {
		return black_avatar;
	}

	public void setBlackAvatar(String black_avatar) {
		this.black_avatar = black_avatar;
	}

	public String getStartingFenPosition() {
		return getSafeValue(starting_fen_position);
	}

	public void setStartingFenPosition(String starting_fen_position) {
		this.starting_fen_position = starting_fen_position;
	}

	public String getMoveList() {
		return getSafeValue(move_list);
	}

	public void setMoveList(String move_list) {
		this.move_list = move_list;
	}

	public String getName() {
		return getSafeValue(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasNewMessage() {
		return has_new_message;
	}

	public void setHasNewMessage(boolean has_new_message) {
		this.has_new_message = has_new_message;
	}

	public int getIPlayAs() {
		return i_play_as;
	}

	public void setIPlayAs(int i_play_as) {
		this.i_play_as = i_play_as;
	}

	public boolean isRated() {
		return is_rated;
	}

	public void setRated(boolean is_rated) {
		this.is_rated = is_rated;
	}

	public int getDaysPerMove() {
		return days_per_move;
	}

	public void setDaysPerMove(int days_per_move) {
		this.days_per_move = days_per_move;
	}

	public int getBlackUserCountry() {
		return black_country_id;
	}

	public void setBlackUserCountry(int black_user_country) {
		this.black_country_id = black_user_country;
	}

	public int getWhiteUserCountry() {
		return white_country_id;
	}

	public void setWhiteUserCountry(int white_user_country) {
		this.white_country_id = white_user_country;
	}

	public int getWhitePremiumStatus() {
		return white_premium_status;
	}

	public void setWhitePremiumStatus(int white_premium_status) {
		this.white_premium_status = white_premium_status;
	}

	public int getBlackPremiumStatus() {
		return black_premium_status;
	}

	public void setBlackPremiumStatus(int black_premium_status) {
		this.black_premium_status = black_premium_status;
	}

	protected static String getSafeValue(String value) {
		return value == null? Symbol.EMPTY : value;
	}
}

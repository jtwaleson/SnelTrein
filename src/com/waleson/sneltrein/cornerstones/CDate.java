package com.waleson.sneltrein.cornerstones;

import java.util.Calendar;

import android.text.format.Time;
import android.util.Log;

public class CDate implements Comparable<CDate> {
	
	// Sorry for this class. There must be a 
	//  better solution, but I wanted something very simple and was tired
	//  of all the datetime classes around.
	// CDate: custom date 
	
	public int year;
	public int month;
	public int day;
	public int hour;
	public int minute;

	public String getHourMinute() {
		return String.format("%02d", hour) + ":"
				+ String.format("%02d", minute);
	}

	public String getDayMonth() {
		return String.format("%02d", day) + "-" + String.format("%02d", month);
	}

	public CDate(int y, int m, int d, int h, int mi) {
		this.year = y;
		this.month = m;
		this.day = d;
		this.hour = h;
		this.minute = mi;
	}

	public String getDBFormat() {
		return getDayMonth() + " " + getHourMinute();
	}

	public static CDate fromDBFormat(String s) {
		CDate result = CDate.simple();
		String[] p = s.split(" ");

		String[] t = p[1].split(":");

		String[] d = p[0].split("-");

		result.month = Integer.parseInt(d[1]);
		result.day = Integer.parseInt(d[0]);
		result.hour = Integer.parseInt(t[0]);
		result.minute = Integer.parseInt(t[1]);

		return result;

	}

	public CDate(Calendar calendar) {
		readFromCalendar(calendar);
	}

	public boolean sameDay(CDate other) {
		return this.day == other.day && this.month == other.month
				&& this.year == other.year;
	}

	public void readFromCalendar(Calendar calendar) {
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		day = calendar.get(Calendar.DATE);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
	}

	public CDate(Time time) {
		readFromAndroidTime(time);
	}

	public void readFromAndroidTime(Time time) {
		year = time.year;
		month = time.month + 1;
		day = time.monthDay;
		hour = time.hour;
		minute = time.minute;
	}

	public String getISOString(boolean forWeb) {
		CDate copy = new CDate(this);
		Calendar c = this.getCalendar();
		int offsethours = (int) Math.round(c.getTimeZone().getOffset(
				c.getTimeInMillis()) / 3600000.0);
		return copy.year + "-" + String.format("%02d", copy.month) + "-"
				+ String.format("%02d", copy.day) + "T"
				+ String.format("%02d", copy.hour) + ":"
				+ String.format("%02d", copy.minute) + ":00"
				+ (forWeb ? "%2B" : "+") + String.format("%02d", offsethours)
				+ ":00";
	}

	public void print() {
		Log.v("CDATE", this.toString());
	}

	@Override
	public String toString() {
		return year + " " + month + " " + day + " " + hour + " " + minute;
	}

	public Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		return calendar;
	}

	public CDate(CDate date) {
		this.year = date.year;
		this.month = date.month;
		this.day = date.day;
		this.hour = date.hour;
		this.minute = date.minute;
	}

	public static CDate simple() {
		return new CDate(0, 0, 0, 0, 0);
	}

	public static CDate now() {
		return new CDate(Calendar.getInstance());
	}

	public void readHourMinuteFromADString(String adstring) {
		String[] s = adstring.split(" ")[1].split(":");
		this.hour = Integer.parseInt(s[0]);
		this.minute = Integer.parseInt(s[1]);
	}

	public boolean sameTime(CDate other) {
		return hour == other.hour && minute == other.minute;
	}

	@Override
	public int compareTo(CDate that) {
		// TODO Auto-generated method stub
		if (this.year != that.year)
			return this.year - that.year;
		if (this.month != that.month)
			return this.month - that.month;
		if (this.day != that.day)
			return this.day - that.day;
		if (this.hour != that.hour)
			return this.hour - that.hour;
		return this.minute - that.minute;
	}

	public CDate getDifference(CDate other) {
		return getDifference(other.getCalendar().getTimeInMillis());
	}

	public CDate getDifference(Long milli) {
		long diff = milli - this.getCalendar().getTimeInMillis();
		diff = Math.abs(diff);
		CDate res = CDate.simple();
		int a = 1000, b = 60, c = 60, d = 24;

		res.day = (int) (diff / (a * b * c * d));
		diff -= res.day * a * b * c * d;
		res.hour = (int) (diff / (a * b * c));
		diff -= res.hour * a * b * c;
		res.minute = (int) Math.ceil(diff / (a * b));

		return res;
	}

	public long getTimeinMillis() {
		return this.getCalendar().getTimeInMillis();
	}

	public String getDiffString() {
		String res = "";

		if (year > 0)
			res += year + " jaar";
		if (month > 0)
			res += month + " maanden";
		if (day > 0)
			res += day + " dagen ";
		if (hour > 0)
			res += hour + " uur ";

		res += minute + " min";

		return res;
	}

	public void addHours(int hours) {
		Calendar c = this.getCalendar();
		c.add(Calendar.HOUR_OF_DAY, hours);
		this.readFromCalendar(c);
	}

	public void addDays(int days) {
		Calendar c = this.getCalendar();
		c.add(Calendar.DAY_OF_MONTH, days);
		this.readFromCalendar(c);
	}

	public void addMinutes(int minutes) {
		Calendar c = this.getCalendar();
		c.add(Calendar.MINUTE, minutes);
		this.readFromCalendar(c);
	}

	public static CDate parseNSTime(String s) {
		CDate r = CDate.simple();
		r.year = Integer.parseInt(s.substring(0, 4));
		r.month = Integer.parseInt(s.substring(5, 7));
		r.day = Integer.parseInt(s.substring(8, 10));
		r.hour = Integer.parseInt(s.substring(11, 13));
		r.minute = Integer.parseInt(s.substring(14, 16));
		return r;
	}
}

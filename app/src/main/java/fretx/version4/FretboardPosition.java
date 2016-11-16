package fretx.version4;

/**
 * Created by Kickdrum on 16-Nov-16.
 */

public class FretboardPosition {
	protected int string;
	protected int fret;

	public FretboardPosition(int str, int frt) {
		if (str < 1 || str > 6) {
			throw new IllegalArgumentException("String number needs to be between 1-6");
		}
		if (frt < 0 || frt > 4) {
			throw new IllegalArgumentException("Fret number needs to between 0-4");
		}
		string = str;
		fret = frt;
	}

	public FretboardPosition(int midiNote) {
		if (midiNote < 40 || midiNote > 68) {
			throw new IllegalArgumentException("This note is outside the display range of FretX");
		}
		if (midiNote > 59) {
			midiNote++;
		}
		this.fret = (midiNote - 40) % 5;
		this.string = 6 - ((midiNote - 40) / 5);
		//This formula always prefers the open 2nd string to the 4th fret of the 3rd string
		//TODO: find a way to resolve this
	}

	public void setFret(int frt) {
		if (frt < 0 || frt > 4) {
			throw new IllegalArgumentException("Fret number needs to between 0-4");
		}
		fret = frt;
	}

	public void setString(int str) {
		if (str < 1 || str > 6) {
			throw new IllegalArgumentException("String number needs to be between 1-6");
		}
		string = str;
	}

	public int getFret() {
		return fret;
	}

	public int getString() {
		return string;
	}

	public byte getLedAddress(){
		return  Byte.valueOf(Integer.toString(10 * (string - 1) + fret));
	}

	public static int midiNoteToLedAddress(int midiNote){
		FretboardPosition fb = new FretboardPosition(midiNote);
		return fb.getLedAddress();
	}

}

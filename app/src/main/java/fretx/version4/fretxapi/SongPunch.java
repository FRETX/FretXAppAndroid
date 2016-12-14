package fretx.version4.fretxapi;

import rocks.fretx.audioprocessing.Chord;

/**
 * Created by onurb_000 on 14/12/16.
 */

public class SongPunch {
	public int timeMs;
	public String root;
	public String type;
	public byte[] fingering;

	public SongPunch(int timeMs , String root, String type, byte[] fingering){
		this.timeMs = timeMs;
		this.root = root;
		this.type = type;
		this.fingering = fingering;
	}
}

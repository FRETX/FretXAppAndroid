package fretx.version4.fretxapi.song;

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

	@Override
	public String toString() {
		return root + type + "@" + timeMs;
	}
}

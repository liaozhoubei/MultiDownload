package com.example.multidownload.entitis;

/**
 * 
 * 綫程信息類，封裝綫程的ID，綫程的url，綫程開始位置，結束位置，以及已經完成的位置
 *
 */
public class ThreadInfo {
	private int id;
	private String url;
	private int start;
	private int end;
	private int finished;

	public ThreadInfo() {
		super();
	}

	/**
	 * 
	 * @param id
	 *            綫程的ID
	 * @param url
	 *            下載文件的網絡地址
	 * @param start
	 *            綫程下載的開始位置
	 * @param end
	 *            綫程下載的結束位置
	 * @param finished
	 *            綫程已經下載到哪個位置
	 */
	public ThreadInfo(int id, String url, int start, int end, int finished) {
		super();
		this.id = id;
		this.url = url;
		this.start = start;
		this.end = end;
		this.finished = finished;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public int getFinished() {
		return finished;
	}

	public void setFinished(int finished) {
		this.finished = finished;
	}

	@Override
	public String toString() {
		return "ThreadInfo [id=" + id + ", url=" + url + ", start=" + start + ", end=" + end + ", finished=" + finished
				+ "]";
	}

}

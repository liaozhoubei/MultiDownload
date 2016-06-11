package com.example.multidownload.entitis;

import java.io.Serializable;

/**
 * 
 * 封裝所下載文件的信息 將FileInfo序列化，可用於Intent傳遞對象
 *
 */
public class FileInfo implements Serializable {
	private int id;
	private String url;
	private String fileName;
	private int length;
	private int finished;

	public FileInfo() {
		super();
	}

	/**
	 * 
	 * @param id   文件的ID
	 * @param url  文件的下載地址
	 * @param fileName  文件的名字
	 * @param length  文件的總大小
	 * @param finished  文件已經完成了多少
	 */
	public FileInfo(int id, String url, String fileName, int length, int finished) {
		super();
		this.id = id;
		this.url = url;
		this.fileName = fileName;
		this.length = length;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getFinished() {
		return finished;
	}

	public void setFinished(int finished) {
		this.finished = finished;
	}

	@Override
	public String toString() {
		return "FileInfo [id=" + id + ", url=" + url + ", fileName=" + fileName + ", length=" + length + ", finished="
				+ finished + "]";
	}

}

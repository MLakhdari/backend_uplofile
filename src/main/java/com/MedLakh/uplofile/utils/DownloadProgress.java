package com.MedLakh.uplofile.utils;

public class DownloadProgress {

	private Float progress;
	private Long totalBytesTransferred;
	private Long totalBytesToTransfer;
	
	public DownloadProgress() {}

	public DownloadProgress(Float progress, Long totalBytesTransferred, Long totalBytesToTransfer) {
		this.progress = progress;
		this.totalBytesTransferred = totalBytesTransferred;
		this.totalBytesToTransfer = totalBytesToTransfer;
	}

	public Float getProgress() {
		return progress;
	}

	public void setProgress(Float progress) {
		this.progress = progress;
	}

	public Long getTotalBytesTransferred() {
		return totalBytesTransferred;
	}

	public void setTotalBytesTransferred(Long totalBytesTransferred) {
		this.totalBytesTransferred = totalBytesTransferred;
	}

	public Long getTotalBytesToTransfer() {
		return totalBytesToTransfer;
	}

	public void setTotalBytesToTransfer(Long totalBytesToTransfer) {
		this.totalBytesToTransfer = totalBytesToTransfer;
	}
	
}

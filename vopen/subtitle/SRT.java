package vopen.subtitle;

public class SRT {
		private int beginTime;
		private int endTime;
		private String srtBodyCh;
		private String srtBodyEn;
		 
		public int getBeginTime() {
		return beginTime;
		}
		 
		public void setBeginTime(int beginTime) {
		this.beginTime = beginTime;
		}
		 
		public int getEndTime() {
		return endTime;
		}
		 
		public void setEndTime(int endTime) {
		this.endTime = endTime;
		}
		 
		public String getSrtBodyCh() {
		return srtBodyCh;
		}
		 
		public void setSrtBodyCh(String srtBodyCh) {
		this.srtBodyCh = srtBodyCh;
		}
		
		public String getSrtBodyEn() {
			return srtBodyEn;
		}
			 
		public void setSrtBodyEn(String srtBodyEn) {
			this.srtBodyEn = srtBodyEn;
		}
		 
		@Override
		public String toString() {
			return "" + beginTime + ":" + endTime + " Ch:" + srtBodyCh + " En:" + srtBodyEn;
		}
}

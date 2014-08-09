package vn.infory.infory.data;

import java.util.List;

public abstract class Message {

	private int idMessage;
	private String logo;
	private int idShop;
	private String time;
	private String title;
	private String content;
	private int status;
	private String image;
	private int imageWidth;
	private int imageHeight;
	private String video;
	private String videoThumbnail;
	private int videoWidth;
	private int videoHeight;
	private List<buttons> buttons;
	
	public static class buttons {
		private String actionTitle;
		private int actionType;

//		 - **0**: call API
//		 - **1**: Open view Shop Info
//		 - **2**: Open view Shop List
//		 - **3**: Open webview
		
		// action type = 0
		private String url;
		private int method; // 0 : get ; 1: post
		private String params; // json 
		
		// action type = 1
		private int idShop;

		// action type = 2
		private int idPlacelist;
		private String keywords;
		private String idShops;

		// action type = 3 (has attribute "url" like action 0)
		
		public buttons() {
			
		}

		public String getActionTitle() {
			return actionTitle;
		}

		public void setActionTitle(String actionTitle) {
			this.actionTitle = actionTitle;
		}

		public int getActionType() {
			return actionType;
		}

		public void setActionType(int actionType) {
			this.actionType = actionType;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getMethod() {
			return method;
		}

		public void setMethod(int method) {
			this.method = method;
		}

		public String getParams() {
			return params;
		}

		public void setParams(String params) {
			this.params = params;
		}

		public int getIdShop() {
			return idShop;
		}

		public void setIdShop(int idShop) {
			this.idShop = idShop;
		}

		public int getIdPlacelist() {
			return idPlacelist;
		}

		public void setIdPlacelist(int idPlacelist) {
			this.idPlacelist = idPlacelist;
		}

		public String getKeywords() {
			return keywords;
		}

		public void setKeywords(String keywords) {
			this.keywords = keywords;
		}

		public String getIdShops() {
			return idShops;
		}

		public void setIdShops(String idShops) {
			this.idShops = idShops;
		}
	}

	public int getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(int idMessage) {
		this.idMessage = idMessage;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public int getIdShop() {
		return idShop;
	}

	public void setIdShop(int idShop) {
		this.idShop = idShop;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getVideoThumbnail() {
		return videoThumbnail;
	}

	public void setVideoThumbnail(String videoThumbnail) {
		this.videoThumbnail = videoThumbnail;
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

	public List<buttons> getButtons() {
		return buttons;
	}

	public void setButtons(List<buttons> buttons) {
		this.buttons = buttons;
	}
	

}

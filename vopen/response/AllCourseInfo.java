package vopen.response;

import java.util.ArrayList;
import java.util.List;

import com.netease.vopen.pal.Constants;

/**
 * 所有的课程数据
 * @author xali
 */
public class AllCourseInfo{
	
	/**头图*/
	public List<CourseInfo> mToutuList = new ArrayList<CourseInfo>();
	/**最热*/
	public List<CourseInfo> mEnjoyList = new ArrayList<CourseInfo>();
	/**TED*/
	public List<CourseInfo> mTedList = new ArrayList<CourseInfo>();
	/**其他*/
	public List<CourseInfo> mQitaList = new ArrayList<CourseInfo>();
	/**经济*/
	public List<CourseInfo> mJingjiList = new ArrayList<CourseInfo>();
	/**人文*/
	public List<CourseInfo> mRenwenList = new ArrayList<CourseInfo>();
	/**数理*/
	public List<CourseInfo> mShuliList = new ArrayList<CourseInfo>();
	/**心里*/
	public List<CourseInfo> mXinliList = new ArrayList<CourseInfo>();
	/**哲学*/
	public List<CourseInfo> mZhexueList = new ArrayList<CourseInfo>();
	
   
    
    public void addCourse(CourseInfo info, String type){
    	if(Constants.DATA_TYPE_TOUTU.equals(type)){
    		mToutuList.add(info);
    	}else if(Constants.DATA_TYPE_ENJOY.equals(type)){
    		mEnjoyList.add(info);
    	}else if(Constants.DATA_TYPE_TED.equals(type)){
    		mTedList.add(info);
    	}else if(Constants.DATA_TYPE_QITA.equals(type)){
    		mQitaList.add(info);
    	}else if(Constants.DATA_TYPE_JINGJI.equals(type)){
    		mJingjiList.add(info);
    	}else if(Constants.DATA_TYPE_RENWEN.equals(type)){
    		mRenwenList.add(info);
    	}else if(Constants.DATA_TYPE_SHULI.equals(type)){
    		mShuliList.add(info);
    	}else if(Constants.DATA_TYPE_XINLI.equals(type)){
    		mXinliList.add(info);
    	}else if(Constants.DATA_TYPE_ZHEXUE.equals(type)){
    		mZhexueList.add(info);
    	}
    }
    
    public List<CourseInfo> getDataFromType(String type){
    	List<CourseInfo> list = null;
    	if(Constants.DATA_TYPE_TOUTU.equals(type)){
    		list = mToutuList;
    	}else if(Constants.DATA_TYPE_ENJOY.equals(type)){
    		list = mEnjoyList;
    	}else if(Constants.DATA_TYPE_TED.equals(type)){
    		list = mTedList;
    	}else if(Constants.DATA_TYPE_QITA.equals(type)){
    		list = mQitaList;
    	}else if(Constants.DATA_TYPE_JINGJI.equals(type)){
    		list = mJingjiList;
    	}else if(Constants.DATA_TYPE_RENWEN.equals(type)){
    		list = mRenwenList;
    	}else if(Constants.DATA_TYPE_SHULI.equals(type)){
    		list = mShuliList;
    	}else if(Constants.DATA_TYPE_XINLI.equals(type)){
    		list = mXinliList;
    	}else if(Constants.DATA_TYPE_ZHEXUE.equals(type)){
    		list = mZhexueList;
    	}
    	return list;
    }
    

    
    public void clear() {
    	mToutuList.clear();
    	mEnjoyList.clear();
    	mTedList.clear();
    	mQitaList.clear();
    	mJingjiList.clear();
    	mRenwenList.clear();
    	mShuliList.clear();
    	mXinliList.clear();
    	mZhexueList.clear();
    }
    
}

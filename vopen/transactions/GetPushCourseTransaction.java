
package vopen.transactions;

import common.framework.http.HttpRequest;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.util.NameValuePair;
import common.util.StringUtil;

import org.json.JSONObject;

import vopen.db.DBApi.CollectInfo;
import vopen.protocol.VopenProtocol;
import vopen.protocol.VopenServiceCode;
import vopen.response.PushList;

import android.content.Context;

import java.util.List;

public class GetPushCourseTransaction extends BaseTransaction {
    String mLatestPushId;
    Context mContext;

    public GetPushCourseTransaction(TransactionEngine transMgr, Context context, String latestPushId) {
        super(transMgr, BaseTransaction.TRANSACTION_TYPE_GET_PUSH_COURSE);
        // TODO Auto-generated constructor stub
        mLatestPushId = latestPushId;
        mContext = context;
    }

    @Override
    public void onResponseSuccess(String response, NameValuePair[] pairs) {
        // TODO Auto-generated method stub
        PalLog.d("GetPushCourseTransaction", response);

        PushList pushList = new PushList();
        JSONObject jsonobj = StringUtil.StringToJson(response);
        pushList.parseJson(jsonobj);
//        /**test**/
//        String testInfo = "{\"pushresult\":[{\"content\":\"测试1:北京师范大学公开课：从爱因斯坦到霍金的宇宙\",\"videoid\":\"M7GF3F8DJ\"},{\"content\":\"测试2：剑桥大学公开课：人类学\",\"videoid\":\"M6P4AQESM\"}]}";
////        Log.d("okry", testInfo);
//        JSONObject jsonobj = StringUtil.StringToJson(testInfo);
//        pushList.parseJson(jsonobj);
//        /**test**/
        notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, pushList);
    }

    @Override
    public void onTransact() {
        // TODO Auto-generated method stub
        HttpRequest request = VopenProtocol.getInstance().createGetPushCourseRequest(mLatestPushId);
        if (!isCancel()) {
            sendRequest(request);
        } else {
            getTransactionEngine().endTransaction(this);
        }
    }

    /**检查是否收藏*/
    public boolean favorExist(List<CollectInfo> favorsdata,
            String courseid) {
        for(CollectInfo info:favorsdata){
            String c_id = info.mCourse_id;
            if(courseid.equals(c_id)){
                return true;
            }
        }
        return false;
    }
    
}

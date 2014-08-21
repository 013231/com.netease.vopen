package vopen.transactions;

import vopen.protocol.VopenServiceCode;
import vopen.response.UiEventTransport;

import common.framework.task.TransactionEngine;
import common.util.NameValuePair;

public class UiEventTransportTransaction extends BaseTransaction {
    private UiEventTransport mEvent;
    public UiEventTransportTransaction(TransactionEngine transMgr,UiEventTransport event) {
		super(transMgr, TRANSACTION_TYPE_UI_EVENT);
		// TODO Auto-generated constructor stub
		mEvent = event;
	}

	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTransact() {
		// TODO Auto-generated method stub
		if (!isCancel()) {
			if(null != mEvent) {
				notifyMessage(VopenServiceCode.TRANSACTION_SUCCESS, mEvent);
				getTransactionEngine().endTransaction(this);
			}
		}else{
			 getTransactionEngine().endTransaction(this);
		}

	}

}

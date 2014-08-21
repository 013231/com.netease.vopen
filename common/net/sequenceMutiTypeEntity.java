package common.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.entity.AbstractHttpEntity;


public class sequenceMutiTypeEntity extends AbstractHttpEntity {
	private List<StepParameter> mParamList = new LinkedList<StepParameter>();
	
	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(int i = 0; i < mParamList.size(); i++){
        	StepParameter p = mParamList.get(i);
        	if(p.mType == TYPE_BYTEARRAY){
        		baos.write((byte[])p.mContent, 0, 
        		           p.mLength > 0 ? p.mLength : ((byte[])p.mContent).length);
        	}
        	else if(p.mType == TYPE_INPUTSTREAM){
        		InputStream instream = (InputStream)p.mContent;
                byte[] buffer = new byte[2048];
                int l;
                if (p.mLength < 0) {
                    while ((l = instream.read(buffer)) != -1) {
                    	baos.write(buffer, 0, l);
                    }
                } else {
                    // consume no more than length
                    long remaining = p.mLength;
                    while (remaining > 0) {
                        l = instream.read(buffer, 0, (int)Math.min(2048, remaining));
                        if (l == -1) {
                            break;
                        }
                        baos.write(buffer, 0, l);
                        remaining -= l;
                    }
                }
        	}
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return bais;
	}

	@Override
	public long getContentLength() {
		int length = 0;
		for (int i = 0; i < mParamList.size(); i++) {
			StepParameter p = mParamList.get(i);
			if(p.mLength < 0 && p.mType == TYPE_BYTEARRAY)
				length += ((byte[])p.mContent).length;
			else
				length += p.mLength;
		}
		
		return length;
	}

	@Override
	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
		
		for(int i = 0; i < mParamList.size(); i++){
        	StepParameter p = mParamList.get(i);
        	if(p.mType == TYPE_BYTEARRAY){
        		outstream.write((byte[])p.mContent, 0, 
        		           p.mLength > 0 ? p.mLength : ((byte[])p.mContent).length);
        	}
        	else if(p.mType == TYPE_INPUTSTREAM){
        		InputStream instream = (InputStream)p.mContent;
        		if(instream == null)
        			continue;
        		
                byte[] buffer = new byte[2048];
                int l;
                if (p.mLength < 0) {
                	p.mLength = 0;
                    while ((l = instream.read(buffer)) != -1) {
                    	outstream.write(buffer, 0, l);
                    	p.mLength += l;
                    }
                } else {
                    // consume no more than length
                    long remaining = p.mLength;
                    while (remaining > 0) {
                        l = instream.read(buffer, 0, (int)Math.min(2048, remaining));
                        if (l == -1) {
                            break;
                        }
                        outstream.write(buffer, 0, l);
                        remaining -= l;
                    }
                }
                
                instream.close();
        	}
        }	
		outstream.flush();
	}

	public static final int TYPE_BYTEARRAY = 1;
	public static final int TYPE_INPUTSTREAM  =2;
	
	public void addByteArray(byte[] array, int len){
		if(array == null)
			return;
		
		StepParameter param = new StepParameter(TYPE_BYTEARRAY);
		param.mContent = array;
		param.mLength = len;
		mParamList.add(param);
	}
	public void addInputStream(InputStream in, int len){
		if(in == null)
			return;
		
		StepParameter param = new StepParameter(TYPE_INPUTSTREAM);
		param.mContent = in;
		param.mLength = len;
		mParamList.add(param);
	}
	
	public void addString(String s){
		if(s == null)
			return;
		
        byte[] content =  s.getBytes();
        addByteArray(content, content.length);		
	}
	
	public class StepParameter{
		public StepParameter(int type){
			mType = type;
		}
		private int mType;
		private Object mContent;
		private int mLength; 
	}
}

package common.pal;

import org.apache.http.entity.ByteArrayEntity;

public class PalByteArrayEntity extends ByteArrayEntity {

	public PalByteArrayEntity(byte[] b,String contentType) {
		super(b);		
		if (null !=contentType) {
			setContentType(contentType);
		}
		// TODO Auto-generated constructor stub
	}
}

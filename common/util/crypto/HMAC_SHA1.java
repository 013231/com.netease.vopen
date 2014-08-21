package common.util.crypto;

import common.util.Base64;


public class HMAC_SHA1
{

	/** ISO-8859-1 or US-ASCII would work, too. */
	private static final String ENCODING = "utf-8";

	HMac mHmac;

	public void init(byte[] keyBytes)
	{
		mHmac = new HMac(new SHA1Digest());
		KeyParameter key = new KeyParameter(keyBytes);
		mHmac.init(key);
	}

	public String getSignature(String baseString)
	{
	
		byte[] data = computeSignature(baseString);
		byte[] b2 = Base64.encode(data);
		try
		{
			return new String( b2,ENCODING);	
		} catch (Exception e)
		{
			// TODO: handle exception
			System.err.println(e + "");
		}
		return  new String( b2);
	}

	public byte[] computeSignature(String baseString)
	{
		mHmac.reset();
		byte[] resBuf = new byte[mHmac.getMacSize()];
		byte[] text = getBytes(baseString);
		mHmac.update(text, 0, text.length);
		mHmac.doFinal(resBuf, 0);
		return resBuf;
	}

	private byte[] getBytes(String string)
	{
		try
		{
			byte[] text = string.getBytes(ENCODING);
			return text;
		} catch (Exception e)
		{
			// TODO: handle exception
			return null;
		}
	}
}

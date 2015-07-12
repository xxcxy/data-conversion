package com.customtime.data.conversion.domain.temporary;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.recode.Recode;

public class LBQStorage implements Temporary{
	private static final Log log = LogFactory.getLog(LBQStorage.class);
	private CustomLinkedBlockingQueue<Recode> lbq = null;
	private long lineRx = 0;
	private long lineTx = 0;
	private final int waitTime = 3000;
	private boolean pushClosed = false;
	private Recode makeReocde;
	private Information info=new Information("test");
	
	public Temporary setMakeObject(Recode recode) {
		this.makeReocde=recode;
		return this;
	}

	public boolean isPushClosed() {
		return pushClosed;
	}

	public String info() {
		return lineRx + ":" + lineTx;
	}

	public boolean init(String id, int lineLimit, int byteLimit,
			int destructLimit) {
		lbq = new CustomLinkedBlockingQueue<Recode>(lineLimit);
		return true;
	}

	public boolean push(Recode recode) {
		if(isPushClosed())
			return false;
		try {
			while(lbq.offer(recode, waitTime, TimeUnit.MILLISECONDS) == false){
//				getStat().incLineRRefused(1);
			}
		} catch (InterruptedException e) {
			return false;
		}
		++lineRx;
		if(lineRx%10000==0)
			log.info(info());
//		getStat().incLineRx(1);
//		getStat().incByteRx(recode.length());
		return true;
	}

	public boolean push(Recode[] recodes, int size) {
		if(isPushClosed())
			return false;
		try {
			Recode[] tRecodes = recodes;
			if(recodes.length>size){
				tRecodes = new Recode[size];
				for(int i=0;i<size;i++)
					tRecodes[i]=recodes[i];
			}
			while (lbq.offer(tRecodes, waitTime, TimeUnit.MILLISECONDS) == false) {
//				getStat().incLineRRefused(1);
//				if (getDestructLimit() > 0 && getStat().getLineRRefused() >= getDestructLimit()){
//                	if (!isPushClosed()){
//                		log.warn("Close LBQStorage for " + getStat().getId() + ". Queue:" + info() + " Timeout times:" + getStat().getLineRRefused());
//                        setPushClosed();
//                	}
//                    return false;
//                }
			
			}
		} catch (InterruptedException e) {
			return false;
		}
		getStat().incLineRx(size);
		lineRx+=size;
		for (int i = 0; i < size; i++) {
			getStat().incByteRx(recodes[i].length());
		}
		return true;
	}

	public boolean fakePush(int lineLength) {
		// TODO Auto-generated method stub
		log.error("this method havenot implement now");
		return false;
	}

	public Recode pull() {
		Recode recode=null;
		try {
			while ((recode = lbq.poll(waitTime,TimeUnit.MILLISECONDS,makeReocde)) == null) {
//				getStat().incLineTRefused(1);
			}
			if (recode != makeReocde) {
				++lineTx;
//				getStat().incLineTx(1);
//				getStat().incByteTx(recode.length());
				return recode;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public int pull(Recode[] recodes) {
		int length = 0;
		try {
			while ((length = lbq.poll(recodes,waitTime,TimeUnit.MILLISECONDS)) == 0) {
				getStat().incLineTRefused(1);
			}
			if (length > 0) {
				lineTx+=length;
				getStat().incLineTx(length);
				for(int i=0;i<length;i++)
					getStat().incByteTx(recodes[i].length());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
		if(length == -1){
			return 0;
		}
		return length;
	}

	public int size() {
		return lbq.size();
	}

	public boolean empty() {
		return (size() <= 0);
	}

	public int getLineLimit() {
		return 0;
	}
	
	/**
	 * Set push state closed.
	 * 
	 * @param close
	 * 			A boolean value represents the wanted state of push.
	 * 
	 */
	public void setPushClosed() {
		pushClosed = true;
		lbq.close();
	}
	
	Information getStat(){
		return info;
	}

}

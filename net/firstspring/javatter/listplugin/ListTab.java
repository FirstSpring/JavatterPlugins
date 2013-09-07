package net.firstspring.javatter.listplugin;

import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import twitter4j.Paging;
import twitter4j.Status;

import com.orekyuu.javatter.account.TwitterManager;
import com.orekyuu.javatter.plugin.TweetObjectBuilder;
import com.orekyuu.javatter.util.BackGroundColor;
import com.orekyuu.javatter.util.TweetObjectFactory;
import com.orekyuu.javatter.view.IJavatterTab;
import com.orekyuu.javatter.viewobserver.UserEventViewObserver;

public class ListTab implements IJavatterTab, AdjustmentListener {

	UserEventViewObserver observer;
	List<TweetObjectBuilder> builders;
	
	JPanel panel;
	ListTabScrollPane tp;

	volatile Queue<JPanel> queue=new ConcurrentLinkedQueue<JPanel>();
	boolean queueFlag;
	boolean queueEvent;
	JPanel last;
	
	public static final Timer refresher = new Timer();
	public RefreshTask refreshTask = new RefreshTask();
	public int listId;
	public String listName = "Loading";
	public Status lastStat;
	
	public ListTab(UserEventViewObserver observer, List<TweetObjectBuilder> builders){
		this.observer = observer;
		this.builders = builders;
		panel = new JPanel();
		panel.setBackground(BackGroundColor.color);
		panel.setLayout(new BoxLayout(panel, 3));
		tp = new ListTabScrollPane(this);
		tp.setViewportView(this.panel);
		tp.getVerticalScrollBar().setUnitIncrement(20);
		tp.getVerticalScrollBar().addAdjustmentListener(this);
	}
	
	class RefreshTask extends TimerTask
	{

		@Override
		public void run()
		{
			try
			{
				List<Status> l = TwitterManager.getInstance().getTwitter().getUserListStatuses(listId, new Paging(1, 200, lastStat.getId()));
				for(int i = l.size() - 1; i >= 0; i--)
				{
					addStatus(l.get(i));
				}
				lastStat = l.get(0);
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public void addStatus(Status stat)
	{
		if(tp.getVerticalScrollBar().getValue()==0&&!queueFlag){
			addObject(createObject(stat));
		}else{
			queue.add(createObject(stat));
			setNumber(queue.size());
		}
	}
	
	public synchronized void setNumber(int num){
		JTabbedPane tab=(JTabbedPane) tp.getParent();
		for(int i=0;i<tab.getTabCount();i++){
			if(tab.getComponentAt(i) == this.tp){
				if(num!=0){
					tab.setTitleAt(i, listName + "("+num+")");
				}else{
					tab.setTitleAt(i, listName);
				}
			}
		}
	}

	@Override
	public Component getComponent() {
		return tp;
	}

	private JPanel createObject(Status status){
		TweetObjectFactory factory = new TweetObjectFactory(status,builders);
		return (JPanel) factory.createTweetObject(this.observer).getComponent();
	}

	private synchronized void addObject(JPanel jpanel){
		jpanel.updateUI();
		this.panel.add(jpanel, 0);
		this.panel.updateUI();
		last = jpanel;
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		if(arg0.getValue()==0){
			if(queueEvent){
				return;
			}
			queueEvent = true;
			Thread th=new Thread(){
				@Override
				public void run(){
					queueFlag=true;
					JPanel lastPanel = last;
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, 3));
					while(!queue.isEmpty()){
						panel.add(queue.poll(), 0);
					}
					addObject(panel);
					setNumber(0);
					queueFlag=false;
					if(lastPanel != null){
						tp.validate();
						tp.getVerticalScrollBar().setValue(lastPanel.getLocation().y);
					}
				}
			};
			th.start();
		}
		else{
			queueEvent = false;
		}
	}

}

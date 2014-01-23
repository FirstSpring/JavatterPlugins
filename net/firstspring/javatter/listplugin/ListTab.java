package net.firstspring.javatter.listplugin;

import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import twitter4j.Paging;
import twitter4j.Status;

import com.orekyuu.javatter.account.TwitterManager;
import com.orekyuu.javatter.plugin.TweetObjectBuilder;
import com.orekyuu.javatter.util.BackGroundColor;
import com.orekyuu.javatter.util.TweetObjectFactory;
import com.orekyuu.javatter.view.IJavatterTab;
import com.orekyuu.javatter.viewobserver.UserEventViewObserver;

public class ListTab implements IJavatterTab, AdjustmentListener
{

	UserEventViewObserver observer;
	List<TweetObjectBuilder> builders;

	TimelineTable table;
	ListTabScrollPane tp;

	volatile Queue<JPanel> queue = new ConcurrentLinkedQueue<JPanel>();
	boolean queueFlag;
	boolean queueEvent;
	JPanel last;

	public static final Timer refresher = new Timer();
	public RefreshTask refreshTask = new RefreshTask();
	public int listId;
	public String listName = "Loading";
	public Status lastStat;

	public ListTab(UserEventViewObserver observer, List<TweetObjectBuilder> builders)
	{
		this.observer = observer;
		this.builders = builders;
		table = new TimelineTable();
		table.setBackground(BackGroundColor.color);
		tp = new ListTabScrollPane(this);
		tp.setViewportView(this.table);
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
				lastStat = l.get(0);
				Collections.reverse(l);
				for (Status s : l)
				{
					addStatus(s);
				}
			}
			catch (Exception e)
			{

			}
		}
	}

	public void addStatus(Status stat)
	{
		final JPanel p = createObject(stat);
		if (tp.getVerticalScrollBar().getValue() == 0 && !queueFlag)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					table.addTop(p);
				}
			});
		}
		else
		{
			queue.add(p);
			setNumber(queue.size());
		}
	}

	public synchronized void setNumber(int num)
	{
		JTabbedPane tab = (JTabbedPane) tp.getParent();
		for (int i = 0; i < tab.getTabCount(); i++)
		{
			if (tab.getComponentAt(i) == this.tp)
			{
				if (num != 0)
				{
					tab.setTitleAt(i, listName + "(" + num + ")");
				}
				else
				{
					tab.setTitleAt(i, listName);
				}
			}
		}
	}

	@Override
	public Component getComponent()
	{
		return tp;
	}

	private JPanel createObject(Status status)
	{
		TweetObjectFactory factory = new TweetObjectFactory(status, builders);
		return (JPanel) factory.createTweetObject(this.observer).getComponent();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0)
	{
		if (arg0.getValue() == 0)
		{
			if (queueEvent)
			{
				return;
			}
			queueEvent = true;
			Thread th = new Thread()
			{
				@Override
				public void run()
				{
					queueFlag = true;
					final Queue<JPanel> q = new LinkedList<JPanel>();
					while (!queue.isEmpty())
					{
						q.add(queue.poll());
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							int c = q.size();
							int height = 0;
							while (!q.isEmpty())
							{
								table.addTop(q.poll());
							}
							for (int i = 0; i < c; i++)
							{
								table.prepareRenderer(ListTab.this.table.getCellRenderer(i, 0), i, 0);
								height += table.getRowHeight(i);
							}
							tp.getVerticalScrollBar().setValue(height);
							setNumber(0);
							queueFlag = false;
						}
					});
				}
			};
			th.start();
		}
		else
		{
			queueEvent = false;
		}
	}

}

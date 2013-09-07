package net.firstspring.javatter.listplugin;

import javax.swing.JScrollPane;

public class ListTabScrollPane extends JScrollPane
{
	ListTab tab;
	
	ListTabScrollPane(ListTab tab)
	{
		super(22, 31);
		this.tab = tab;
	}
	
}

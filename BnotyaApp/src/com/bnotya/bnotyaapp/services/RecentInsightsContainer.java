package com.bnotya.bnotyaapp.services;

import java.util.LinkedList;
import java.util.Queue;

public class RecentInsightsContainer
{
	public static Queue<Integer> recentInsights = new LinkedList<Integer>();
	
	public static int getRandomId(int max)
	{
		// Random from 1 to max
		int result = 0;
		result += (Math.random() * max);
		return result;
	}
}

/**
 * Author: Nanxi Kang (nkang@cs.princeton.edu) 
 * All rights reserved.
 */
import java.io.*;
import java.util.StringTokenizer;

public class Procedure {

	public static double RATIO[] = 
		{1.0, 1.2, 1.4, 1.6, 1.8, 2.0, 2.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5};
		//{1.0, 1.1, 1.2, 1.4, 1.6, 1.8, 2.0, 2.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5};
	
	public static double CUS_RATIO[] = RATIO;//{1.0, 1.3, 1.95};
	public static int CUS_HOP[] = {1, 5, 10, 15};	
	
	public static boolean INIT_ID = false;
	
	//Input: real path policy, ratio
	//Output: estimated PolicyNum
	public static void Control(String realPolicy, String ratio, String policyNum) throws Exception {
		
		BufferedReader f_policy = new BufferedReader(new FileReader(realPolicy));
		BufferedReader f_ratio = new BufferedReader(new FileReader(ratio));
		PrintWriter fout = new PrintWriter(new FileWriter(policyNum));
		
		int n_policy;
		int policy_size;
		double r;
		
		n_policy = Integer.parseInt(f_policy.readLine());
		if (Integer.parseInt(f_ratio.readLine()) != n_policy) {
			throw new Exception();
		}
		fout.println(n_policy);
		
		for (int i = 0; i < n_policy; ++i) {
			policy_size = Integer.parseInt(f_policy.readLine());
			r = Double.parseDouble(f_ratio.readLine());
			fout.println((int)(policy_size * r));			
		}
		
		f_policy.close();
		f_ratio.close();
		fout.close();
	}
	
	
	//Input: real path policy
	//Output: ratio
	public static void InitRatio(String realPolicy, String Topology, String ratio) throws Exception {
			
		BufferedReader f_policy = new BufferedReader(new FileReader(realPolicy));
		BufferedReader f_topo = new BufferedReader(new FileReader(Topology));
		
		PrintWriter fout = new PrintWriter(new FileWriter(ratio));
		
			
		int n_policy;
		n_policy = Integer.parseInt(f_policy.readLine());
		fout.println(n_policy);
		
		for (int i = 0; i < 4; ++i)
			f_topo.readLine();
		for (int i = 0; i < n_policy; ++i) {
			if (INIT_ID)
				fout.println(RATIO[0]);
			else {
				StringTokenizer tokens = new StringTokenizer(f_topo.readLine());
				int hop = Integer.parseInt(tokens.nextToken());
				for (int j = 0; j < CUS_HOP.length; ++j)
					if (hop <= CUS_HOP[j]) {
						fout.println(CUS_RATIO[j]);
						break;
					}
			}
		}
			
			
		f_topo.close();
		f_policy.close();
		fout.close();
	}
	
	
	// Reads the resFiles generated by path algorithm, determines the next estimation ratio for failed paths.
	// Summaries the results for multiple processes (path algorithms) to a single file (now_res)
	//Return: 0 for try again, 1 for success, -1 for failure 
	public static int CheckAdjustP(String[] resFiles, String now_res, String now_ratio, String next_ratio) throws Exception {
		BufferedReader f_ratio = new BufferedReader(new FileReader(now_ratio));
		PrintWriter fout = new PrintWriter(new FileWriter(next_ratio));
		PrintWriter res_out = new PrintWriter(new FileWriter(now_res));
		
		int n_policy = Integer.parseInt(f_ratio.readLine());
		fout.println(n_policy);
		res_out.println(n_policy);
			
		
		//0 for try again, 1 for success, -1 for failure
		int status = 1;
		int failure_times = 0;
		
		BufferedReader f_res = null;
		for (int i = 0, l = 0; status >= 0 && l < resFiles.length; ++l) {
			f_res = new BufferedReader(new FileReader(resFiles[l]));
			if (Integer.parseInt(f_res.readLine()) != n_policy) {
				throw new Exception();
			}
				
			while (i < n_policy) {
				String s = f_res.readLine();
				res_out.println(s);
				
				String ss = f_res.readLine();
				if (ss == null) {
					f_res.close();
					System.out.println(resFiles[l] + " >  " + s);
					break;
				}
				res_out.println(ss);
				
				
				++i;
				int success = Integer.parseInt(ss);			
				s = f_res.readLine();
				res_out.println(s);
					
				double r = Double.parseDouble(f_ratio.readLine());
								
				if (success == 0) {
					status = 0;
					System.out.println("FAILED : " + r);
					++failure_times;
					for (int j = 0; j < RATIO.length; ++j) 
						if (RATIO[j] > r + 1e-6) {
							r = RATIO[j];
							break;
						}
						else if (j == RATIO.length - 1) {
							status = -1;
						}
						
					if (status < 0)
						break;				
				}
					
				fout.println(r);
			}
		}
			
		System.out.println("Failure Times : " + failure_times);
		
		fout.println();
			
		if (f_res != null)
			f_res.close();
		f_ratio.close();
		fout.close();
			
		if (status != 0) {
			File f = new File(next_ratio);
			f.delete();
		}
			
		return status;
	}
	
}

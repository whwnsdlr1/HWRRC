package com.remotecontrol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import android.os.Environment;

public class CR
{
	public final static int NUMBER_OF_LETTER = 37;
	private final int PART_MIN_X = 150;
	private final int PART_MIN_Y = 120;
	private final int PART_MAX_X = 1050;
	private final int PART_MAX_Y = 520;
	
	private final int diff_letter_tolerance = 15; // 나중에 상수 말고 변수로 글씨 크기에 따라 조정하는 방법도 생각
	
	private LinkedList<file_read_help_class>[] database;
	private Vector< LinkedList<coord>> v_stroke;
	private Vector< LinkedList<coord>> v_stroke_part[];
	private Vector< LinkedList<info_dir>> v_stroke_dir;
	private Vector< LinkedList<info_dir>> v_stroke_dir_part[];
	private int[] training_data_count;
	private static String[] letter = new String[ NUMBER_OF_LETTER];
	private int partition_mode;
	
	private int blob_count;
	private LinkedList<Integer> blob_endpoint;
	
	public CR()
	{
		v_stroke_dir = new Vector< LinkedList<info_dir>>( 10, 30);
		database = new LinkedList[ NUMBER_OF_LETTER];
		for( int n = 0 ; n < NUMBER_OF_LETTER ; n++)
			database[n] = new LinkedList<file_read_help_class>();
		v_stroke_part = new Vector[5];
		for( int i = 0 ; i < 5 ; i++)
			v_stroke_part[i] = new Vector< LinkedList<coord>>();
		v_stroke_dir_part = new Vector[5];
		for( int i = 0 ; i < 5 ; i++)
			v_stroke_dir_part[i] = new Vector< LinkedList<info_dir>>();
		init();
	}
	
	private void init()
	{
		training_data_count = new int[ NUMBER_OF_LETTER];
		for( int n = 0 ; n < NUMBER_OF_LETTER ; n++)
			training_data_count[ n] = 0;
		loadData();
	}
	
	private void loadData()
	{
		String sdPath;
		String externalState = Environment.getExternalStorageState();
		if( externalState.equals( Environment.MEDIA_MOUNTED))
			sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		else
			sdPath = Environment.MEDIA_UNMOUNTED;
		
		File directory = new File( sdPath + "/train_data");
		
		if( directory.isDirectory() == false)
		{
			//error 체크
		}
		else
		{
			try {
			File[] sub_file = directory.listFiles();
			for( File f : sub_file)
			{
				if( f.isFile() == true)
				{
					if( f.getName().startsWith( "CR_"))
					{
						BufferedReader inFile = new BufferedReader(new FileReader( f));
						String sLine = null;
						boolean unconditional;
						
						sLine = inFile.readLine();
						training_data_count[ find_letter_index( f.getName().substring( 3))] = Integer.parseInt( sLine.substring( 1));

						while( (sLine = inFile.readLine()) != null )
						{
							if( sLine.length() < 2)
								break;
							
							unconditional = false;
							if( sLine.charAt( 0) == '=')
							{
								unconditional = true;
								sLine = sLine.substring( 1);
							}
							
					    	file_read_help_class frhc = new file_read_help_class(); 
					    	frhc.dir_length = new LinkedList<Integer>();
					    	
					    	StringTokenizer st = new StringTokenizer( sLine, "/");
					    	int phase = 0;
					    	int sum = 0;
					    	int n = 0;
					    	while( st.hasMoreElements())
					    	{ /////////////////////////// dis 일때 길이일때 카운트일때 다 따로
					    		String str_token = st.nextToken();
					    		StringTokenizer st2 = new StringTokenizer( str_token, " ");
					    		while( st2.hasMoreElements())
					    		{
					    			String str_token2 = st2.nextToken();
					    			
					    			if( phase == 0)
					    				frhc.dirs = str_token2;
					    			else if( phase == 1)
					    			{
					    				int i = Integer.parseInt( str_token2);
					    				sum += i;
					    				n++;
					    				frhc.dir_length.add( i);
					    			}
					    			else
					    				frhc.count = Integer.parseInt( str_token2);
					    		}
					    		if( phase == 1)
					    		{
					    			for( int nn = 0 ; nn < n ; nn++)
					    			{
					    				frhc.dir_length.add( nn, (( frhc.dir_length.get( nn) * 100) / sum));
										frhc.dir_length.remove( nn + 1);
					    			}
					    		}
					    		phase++;
					    	}
					    	
					    	int index = find_letter_index( f.getName().substring( 3));
					    	if( frhc.count / (float)training_data_count[ index] > 0.12)
					    		database[index].add( frhc);
					    	else if( unconditional == true)
					    		database[index].add( frhc);
						}
						
						inFile.close();
					}
				}
			}
			} catch (IOException e) {	}
			//_notificationField.setText("[" + Integer.toString( count) + "]개의 글자정보 로딩완료");
		}
	}
	
	public void clearVector()
	{
		Enumeration< LinkedList<info_dir>> my_enum2 = v_stroke_dir.elements();
		while( my_enum2.hasMoreElements()){
			LinkedList<info_dir> temp = my_enum2.nextElement();
			temp.clear();
		}
		v_stroke_dir.clear();
		
		for( int i = 0 ; i < 5 ; i++)
		{		
			my_enum2 = v_stroke_dir_part[ i].elements();
			while( my_enum2.hasMoreElements()){
				LinkedList<info_dir> temp = my_enum2.nextElement();
				temp.clear();
			}
			v_stroke_dir_part[ i].clear();
		}
		
		Enumeration< LinkedList<coord>> my_enum3;
		for( int i = 0 ; i < 5 ; i++)
		{		
			my_enum3 = v_stroke_part[ i].elements();
			while( my_enum3.hasMoreElements()){
				LinkedList< coord> temp = my_enum3.nextElement();
				temp.clear();
			}
			v_stroke_part[ i].clear();
		}
	}
	
	public int find_letter_index( String str2)
	{
		String str1;
		for( int n = 0 ; n < NUMBER_OF_LETTER ; n++)
		{
			str1 = ( letter[n] + "");
			if( str1.equals( str2))
				return n;
		}
		return NUMBER_OF_LETTER - 1;
	}
	
	public String find_CR( Vector< LinkedList<coord>> VC)
	{
		String result_str = "";
		LinkedList<info_dir> l_dirs;
		
		v_stroke = VC;
		
		if( v_stroke.isEmpty() == true)
			return "입력 없음";
		
		partition_mode = 0;
		Create_v_dir();
		blob_count = 0;
		blob_endpoint = new LinkedList<Integer>();
		
		if( partition_mode == 0)
		{
			blob_labeling( v_stroke);
			for( int process_blob = 0 ; process_blob < blob_count ; process_blob++)
			{
				l_dirs = Get_l_dirs_from_blob( process_blob, v_stroke_dir);
				/*result_str += Find( l_dirs);
				l_dirs.clear();*/
				
				int index;
				if( process_blob == 0)
					index = 0;
				else
					index = blob_endpoint.get( process_blob - 1).intValue() + 1;
				
				String t_t_str = Find( l_dirs);
				t_t_str = exception_letter_process( t_t_str, v_stroke.get(index));
				result_str += t_t_str;
				l_dirs.clear();
			}
			
			blob_endpoint.clear();
			result_str = analyze_letter( result_str);
		}
		else if( partition_mode == 1 || partition_mode == 2)
		{
			result_str = "p2";
			
			for( int n1 = 1 ; n1 < 3 ; n1++)
			{
				blob_labeling( v_stroke_part[ n1]);

				String t_str = ",";
				for( int process_blob = 0 ; process_blob < blob_count ; process_blob++)
				{
					l_dirs = Get_l_dirs_from_blob( process_blob, v_stroke_dir_part[ n1]);
					/*t_str += Find( l_dirs);
					l_dirs.clear();*/

					int index;
					if( process_blob == 0)
						index = 0;
					else
						index = blob_endpoint.get( process_blob - 1).intValue() + 1;
					
					String t_t_str = Find( l_dirs);
					t_t_str = exception_letter_process( t_t_str, v_stroke_part[ n1].get( index));
					t_str += t_t_str;
					l_dirs.clear();
				}
				result_str += t_str;
				blob_endpoint.clear();
			}
		}
		else
		{
			result_str = "p4";
			
			Vector<LinkedList<coord>> temp_v_stroke;
			Vector<LinkedList<info_dir>> temp_v_stroke_dir;
			
			temp_v_stroke = v_stroke_part[2];
			temp_v_stroke_dir = v_stroke_dir_part[2];
			v_stroke_part[2] = v_stroke_part[3];
			v_stroke_dir_part[2] = v_stroke_dir_part[3];
			v_stroke_part[3] = temp_v_stroke;
			v_stroke_dir_part[3] = temp_v_stroke_dir;
			
			for( int n1 = 1 ; n1 < 5 ; n1++)
			{
				blob_labeling( v_stroke_part[ n1]);
				
				String t_str = ",";
				for( int process_blob = 0 ; process_blob < blob_count ; process_blob++)
				{
					l_dirs = Get_l_dirs_from_blob( process_blob, v_stroke_dir_part[ n1]);
					/*t_str += Find( l_dirs);
					l_dirs.clear();*/
					
					int index;
					if( process_blob == 0)
						index = 0;
					else
						index = blob_endpoint.get( process_blob - 1).intValue() + 1;
					
					String t_t_str = Find( l_dirs);
					t_t_str = exception_letter_process( t_t_str, v_stroke_part[ n1].get(index));
					t_str += t_t_str;
					l_dirs.clear();
				}
				result_str += t_str;
				blob_endpoint.clear();
			}
		}
		return result_str;
	}
	
	private String exception_letter_process( String ch, LinkedList<coord> in_l_coord)
	{
		if( ch.equals("0") || ch.equals( "ㅇ"))
		{
			if( Get_length( in_l_coord.getFirst(), in_l_coord.getLast()) > diff_letter_tolerance * 4)
				return "6";
		}
		else if( ch.equals("6"))
		{
			if( Get_length( in_l_coord.getFirst(), in_l_coord.getLast()) < diff_letter_tolerance * 4)
				return "0";
			else
			{
				Direction dir = Get_direction( in_l_coord.getFirst(), in_l_coord.getLast());
				if( dir == Direction.DIR3 || dir == Direction.DIR2)
					return "V";
			}
		}
		else if( ch.equals("V"))
		{
			Direction dir = Get_direction( in_l_coord.getFirst(), in_l_coord.getLast());
			if( dir != Direction.DIR3 && dir != Direction.DIR2 && dir != Direction.DIR4)
				return "6";
			else if( Get_length( in_l_coord.getFirst(), in_l_coord.getLast()) < diff_letter_tolerance * 4)
				return "0";
		}
		else if( ch.equals("2"))
		{
			Direction dir = Get_direction( in_l_coord.get( Math.max( in_l_coord.size() -2, 0)), in_l_coord.getLast());
			if( dir != Direction.DIR3 && dir != Direction.DIR2 && dir != Direction.DIR4)
				return "3";
			return ch;
		}
		else if( ch.equals("3"))
		{
			if( Get_length( in_l_coord.getFirst(), in_l_coord.getLast()) < diff_letter_tolerance * 4)
				return "8";
			return ch;
		}

		return ch;
	}
	
	private String analyze_letter( String in_str)	// 여기서 부터 수정 시작
	{
		String str = in_str.charAt( 0) + "";
		int letter_index;
		
		if( str.equals( "0"))
		{
			if( in_str.length() > 1)
				in_str = in_str.replace( '0', 'ㅇ');
		}
		else if( str.equals("o"))
		{
			if( in_str.length() == 1)
				in_str = "0";
		}
		else if( str.equals("V"))
		{
			if( in_str.length() == 1)
				in_str = "down";
			else
				in_str.replace( 'ㅇ', '0');
		}
		else if( str.equals("ㄴ"))
		{
			if( in_str.length() == 1)
				in_str = "nextskip";
		}
		else if( str.startsWith( "up"))
		{
			if( in_str.length() != 1)
				in_str = "ㅅ" + in_str.substring( 1);
		}
		
		if( in_str.contains( "1ㅂ"))
		{
			in_str = in_str.replace("1ㅂ", "ㅂ");
		}
		if( in_str.contains( "1ㅋ"))
		{
			in_str = in_str.replace("1ㅋ", "ㅂ");
		}
		
		str = in_str.charAt( 0) + "";
		letter_index = find_letter_index( str);
		if( is_Korean_letter( letter_index))
		{
			str = "find,";
			
			in_str = in_str.replace( '0', 'ㅇ');
			str += in_str;
		}
		else if( is_Num_letter( letter_index))
		{
			str = "channel,";
			
			in_str = in_str.replace( 'ㅇ', '0');
			in_str = in_str.replace( 'V', '6');
			str += in_str;
		}
		else if( letter_index == 10)		// 볼륨
		{
			str = "volume,";
			str += in_str.substring( 1);
		}
		else 
			str = in_str;
		
		return str; 
	}
	
	private String Find( LinkedList<info_dir> l_dirs)
	{
		String l_name = "NULL";
		int probability = 0, t_probability, t_probability2;
		
		if( l_dirs.size() == 0)
			return l_name;
		
		int sum = 0;		// 모션에 길이들을 백분율로 표시해줌 ------------------------------------------------
		for( int n1 = 0 ; n1 < l_dirs.size() ; n1++)	
			sum += l_dirs.get( n1).length;
		for( int n1 = 0 ; n1 < l_dirs.size() ; n1++)
			l_dirs.get(n1).length = ( l_dirs.get(n1).length * 100) / sum; 
		//	--------------------------------------------------------------------------------------
		

		for( int n1 = 0 ; n1 < database.length ; n1++)
		{
			for( int n2 = 0 ; n2 < database[ n1].size() ; n2++)
			{
				t_probability = t_probability2 = 0;
				String dirs = database[ n1].get( n2).dirs;

				// ----------------------------------------------------------- 비교 1 ---
				int n3 = 0, n4 = 0;
				while( n3 < dirs.length() && n4 < l_dirs.size())
				{
					Direction dir = Convert_sign_to_dir( dirs.charAt( n3));
					
					if( dir.toString().charAt( 0) == 'T')
					{
						if( l_dirs.get( n4).direction != dir)
						{
							if( l_dirs.get( n4).direction.toString().charAt( 0) == 'T')
								break;
							n4++;
						}
						else
						{
							n3++;
							n4++;
						}
					}
					else
					{
						if( dir == l_dirs.get( n4).direction)
						{
							// t_probability += ( _motion.get( n4).length * database[ n1].get( n2).dir_length.get( n3));
							int temp_sum = l_dirs.get( n4).length - database[ n1].get( n2).dir_length.get( n3);
							temp_sum = ( temp_sum * temp_sum) /2 ;
							t_probability += Math.sqrt( temp_sum);
							n3++;
							n4++;
						}
						else if( ( n3 + 1) < dirs.length())
						{
							Direction dir2 = Convert_sign_to_dir( dirs.charAt( n3 + 1));
							if( dir2 == l_dirs.get( n4).direction || l_dirs.get( n4).direction.toString().charAt( 0) == 'T')
							{
								int temp_sum = database[ n1].get( n2).dir_length.get( n3);
								temp_sum = ( temp_sum * temp_sum) / 2;
								t_probability += Math.sqrt( temp_sum);
								n3++;
							}
							else
								n4++;
						}
						else
						{
							if( l_dirs.get( n4).direction.toString().charAt( 0) == 'T')
								break;
							else
								n4++;
						}
					}
				}
				
				for( ; n3 < dirs.length() ; n3++)
				{
					int temp_sum = database[ n1].get( n2).dir_length.get( n3);
					if( temp_sum == 0)
						temp_sum = 30;
					temp_sum = ( temp_sum * temp_sum) / 2;
					t_probability += Math.sqrt( temp_sum);
				}// ----------------------------------------------------------- 비교 1 ---

				
				// ----------------------------------------------------------- 비교 2 ---
				n3 = n4 = 0;
				while( n3 < dirs.length() && n4 < l_dirs.size())
				{
					Direction dir = Convert_sign_to_dir( dirs.charAt( n3));
					
					if( l_dirs.get( n4).direction.toString().charAt( 0) == 'T')
					{
						if( l_dirs.get( n4).direction != dir)
						{
							if( dir.toString().charAt( 0) == 'T')
								break;
							
							int temp_sum = database[ n1].get( n2).dir_length.get( n3);
							temp_sum = ( temp_sum * temp_sum) / 2;
							t_probability2 += Math.sqrt( temp_sum);
							n3++;
						}
						else
						{
							n3++;
							n4++;
						}
					}
					else
					{
						if( dir == l_dirs.get( n4).direction)
						{
							// t_probability += ( _motion.get( n4).length * database[ n1].get( n2).dir_length.get( n3));
							int temp_sum = l_dirs.get( n4).length - database[ n1].get( n2).dir_length.get( n3);
							temp_sum = ( temp_sum * temp_sum) /2 ;
							t_probability2 += Math.sqrt( temp_sum);
							n3++;
							n4++;
						}
						else if( ( n4 + 1) < l_dirs.size())
						{
							if( dir == l_dirs.get( n4 + 1).direction || dir.toString().charAt( 0) == 'T')
								n4++;
							else
							{
									int temp_sum = database[ n1].get( n2).dir_length.get( n3);
									temp_sum = ( temp_sum * temp_sum) / 2;
									t_probability2 += Math.sqrt( temp_sum);
									n3++;
							}
						}
						else
						{
							if( dir.toString().charAt( 0) == 'T')
								break;
							
							int temp_sum = database[ n1].get( n2).dir_length.get( n3);
							temp_sum = ( temp_sum * temp_sum) / 2;
							t_probability2 += Math.sqrt( temp_sum);
							n3++;
						}
					}
				}
				
				for( ; n3 < dirs.length() ; n3++)
				{
					int temp_sum = database[ n1].get( n2).dir_length.get( n3);
					if( temp_sum == 0)
						temp_sum = 30;
					temp_sum = ( temp_sum * temp_sum) / 2;
					t_probability2 += Math.sqrt( temp_sum);
				}	// ----------------------------------------------------------- 비교 2 ---
				
				
				if( t_probability > t_probability2)
					t_probability = t_probability2;
									
				t_probability = 100 - t_probability;
				if( t_probability > probability)
				{
					probability = t_probability;
					l_name = letter[ n1];
				}
			}
		}
		
		// 바뀐 알고리즘 교체 하는곳 end end end end end end000000000000000000000000000000000000000000000000000
		
		
		
		if( probability < 50)
			l_name = "none";
		
		return l_name;
	}
	
	private void blob_labeling( Vector<LinkedList< coord>> local_v_coord)
	{
		int max_x_1 = 0, min_x_1 = 2000;
		blob_count = 0;
				
		if( local_v_coord.size() != 0)
		{
			blob_count = 1;
			
			if( local_v_coord.size() >= 2)
			{
				for( int n1 = 1 ; n1 < local_v_coord.size() ; n1++)
				{
					int max_x = 0, min_x = 2000;
					for( int n2 = 0 ; n2 < local_v_coord.get( n1 - 1).size() ; n2++)
					{
						if( max_x_1 < local_v_coord.get( n1 - 1).get( n2).x)
							max_x_1 = local_v_coord.get( n1 - 1).get( n2).x;
						if( min_x_1 > local_v_coord.get( n1 - 1).get( n2).x)
							min_x_1 = local_v_coord.get( n1 - 1).get( n2).x;
					}
					
					for( int n2 = 0 ; n2 < local_v_coord.get( n1).size() ; n2++)
					{
						if( max_x < local_v_coord.get( n1).get( n2).x)
							max_x = local_v_coord.get( n1).get( n2).x;
						if( min_x > local_v_coord.get( n1).get( n2).x)
							min_x = local_v_coord.get( n1).get( n2).x;
					}
					
					if( min_x_1 < max_x && max_x_1 > max_x)
					{
					}
					else if( min_x_1 < min_x && max_x_1 > min_x)
					{
					}
					else if( min_x < max_x_1 && max_x > max_x_1)
					{
					}
					else if( min_x < min_x_1 && max_x > min_x_1)
					{
					}
					else if( Math.abs( min_x - max_x_1) < diff_letter_tolerance)
					{
					}
					else if( Math.abs( min_x_1 - max_x) < diff_letter_tolerance)
					{
					}
					else
					{
						max_x_1 = 0;
						min_x_1 = 2000;
						blob_count++;
						blob_endpoint.add( n1 - 1);
					}
				}				
			}
			blob_endpoint.add( local_v_coord.size() - 1);
		}
	}
	
	private LinkedList<info_dir> Get_l_dirs_from_blob( int process_blob, Vector<LinkedList<info_dir>> in_v_dir)
	{
		int n1;
		LinkedList<info_dir> l_dirs = new LinkedList<info_dir>();
		
		if( process_blob == 0)
			n1 = 0;
		else
			n1 = blob_endpoint.get( process_blob - 1).intValue() + 1;
			
		for( ; n1 < blob_endpoint.get( process_blob).intValue() + 1 ; n1++)
		{
			if( n1 < 0 || n1 >= in_v_dir.size())	// 추가 예외처리
				return l_dirs;
			
			LinkedList<info_dir> t_l_dirs = in_v_dir.get( n1);
			
			for( int n2 = 0 ; n2 < t_l_dirs.size() ; n2++)
				l_dirs.add( t_l_dirs.get( n2));
		}

		if( l_dirs.isEmpty() == false)
		{
			if( process_blob != 0 || l_dirs.getFirst().toString().charAt( 0) == 'T')
				l_dirs.remove( 0);
		}
		
		return l_dirs;
	}
	
	private void Create_v_dir()
	{
		int start_index = 0;
		int pre_stroke = 0;
		int divide_x = 0, divide_y = 0, pre_part = 0, now_part = 0;
		boolean is_started = false;
		coord pre_coord = new coord();
		coord now_coord = new coord();
		
		for( int n1 = 0 ; n1 < v_stroke.size() ; n1++)
		{
			LinkedList<coord> l_coord = v_stroke.get( n1);
			
			if( n1 == 0)
			{
				if( l_coord.getFirst().x < PART_MIN_X && l_coord.getLast().x > PART_MAX_X)
				{
					start_index = 1;
					partition_mode = 1;
					divide_y = ( l_coord.getFirst().y + l_coord.getLast().y) / 2; 
					continue;
				}
				else if( l_coord.getFirst().y < PART_MIN_Y && l_coord.getLast().y > PART_MAX_Y)
				{
					start_index = 1;
					partition_mode = 2;
					divide_x = ( l_coord.getFirst().x + l_coord.getLast().x) / 2;
					continue;
				}
			}
			else if( n1 == 1)
			{
				if( l_coord.getFirst().x < PART_MIN_X && l_coord.getLast().x > PART_MAX_X)
				{
					if( partition_mode == 2)
					{
						start_index = 2;
						partition_mode = 3;
						divide_y = ( l_coord.getFirst().y + l_coord.getLast().y) / 2;
						continue;
					}
				}
				else if( l_coord.getFirst().y < PART_MIN_Y && l_coord.getLast().y > PART_MAX_Y)
				{
					if( partition_mode == 1)
					{
						start_index = 2;
						partition_mode = 3;
						divide_x = ( l_coord.getFirst().x + l_coord.getLast().x) / 2;
						continue;
					}
				}
				
				if( partition_mode != 0)
					pre_part = Get_part( l_coord.getFirst(), divide_x, divide_y);
			}
			else if( n1 == 2)
			{
				if( partition_mode == 3)
					pre_part = Get_part( l_coord.getFirst(), divide_x, divide_y);
			}

			if( partition_mode != 0)
			{
				now_part = Get_part( l_coord.getFirst(), divide_x, divide_y);
				
				if( now_part != pre_part)
				{
					is_started = false;
					
					if( v_stroke_dir_part[ pre_part].size() != 0)
						v_stroke_dir_part[ pre_part].clear();
						
					v_stroke_dir_part[ pre_part] = v_stroke_dir;
					v_stroke_dir = new Vector< LinkedList<info_dir>>( 10, 30);
					
					for( int index = start_index ; index < n1; index++)
						v_stroke_part[ pre_part].add( v_stroke.get( index));
					
					start_index = n1;
					pre_part = now_part;
				}
			}
			
			//if( l_coord.size() < 2)	// 나중에 블랍라벨릴ㅇ한후 이 부분에 걸리면 서로 잘리니 확인할것
			//	continue;
			
			LinkedList<info_dir> l_dir = new LinkedList<info_dir>();
			
			pre_coord.x = l_coord.get( 0).x;
			pre_coord.y = l_coord.get( 0).y;
			if( is_started == true)
			{
				Direction dir = Convert_dir_to_T( Get_direction( v_stroke.get( pre_stroke).getLast(), pre_coord));
				info_dir dir_info = new info_dir();
				dir_info.direction = dir;
				dir_info.length = 0;
				l_dir.add( dir_info);
			}
			is_started = true;
			pre_stroke = n1;
				
			v_stroke_dir.add( l_dir);
			for( int n2 = 1 ; n2 < l_coord.size() ; n2++)
			{
				now_coord.x = l_coord.get( n2).x;
				now_coord.y = l_coord.get( n2).y;
				
			Direction dir = Get_direction( pre_coord, now_coord);
				int length = Get_length( pre_coord, now_coord);
				
				if( l_dir.isEmpty() == false && l_dir.getLast().direction == dir)
					l_dir.getLast().length += length;
				else
				{
					info_dir dir_info = new info_dir();
					dir_info.direction = dir;
					dir_info.length = length;
					l_dir.add( dir_info);
				}
				pre_coord.x = now_coord.x;
				pre_coord.y = now_coord.y;
			}
		}
		
		if( partition_mode != 0)
		{
			for( int index = start_index ; index < v_stroke.size() ; index++)
				v_stroke_part[ now_part].add( v_stroke.get( index));
			v_stroke_dir_part[ now_part] = v_stroke_dir;
		}
	}
	
	private int Get_part( coord conm, int divide_x, int divide_y)
	{
		int now_part = 0;
		
		if( partition_mode == 1)
		{
			if( conm.y < divide_y)
				now_part = 1;
			else
				now_part = 2;
		}
		else if( partition_mode == 2)
		{
			if( conm.x < divide_x)
				now_part = 1;
			else
				now_part = 2;
		}
		else if( partition_mode == 3)
		{
			if( conm.x < divide_x)
			{
				if( conm.y < divide_y)
					now_part = 1;
				else
					now_part = 2;
			}
			else
			{
				if( conm.y < divide_y)
					now_part = 3;
				else
					now_part = 4;
			}
		}
		return now_part;
	}
	
	private int Get_length( coord pre, coord cur)
	{
		int dx = cur.x - pre.x;
		int dy = cur.y - pre.y;
		
		return (int)Math.sqrt( dx * dx + dy * dy);
	}
	
	private Direction Convert_sign_to_dir( int ch)
	{
		if( ch == '1')
			return Direction.DIR1;
		else if( ch == '2')
			return Direction.DIR2;
		else if( ch == '3')
			return Direction.DIR3;
		else if( ch == '4')
			return Direction.DIR4;
		else if( ch == '5')
			return Direction.DIR5;
		else if( ch == '6')
			return Direction.DIR6;
		else if( ch == '7')
			return Direction.DIR7;
		else if( ch == '8')
			return Direction.DIR8;
		else if( ch == 'a')
			return Direction.T_DIR1;
		else if( ch == 'b')
			return Direction.T_DIR2;
		else if( ch == 'c')
			return Direction.T_DIR3;
		else if( ch == 'd')
			return Direction.T_DIR4;
		else if( ch == 'e')
			return Direction.T_DIR5;
		else if( ch == 'f')
			return Direction.T_DIR6;
		else if( ch == 'g')
			return Direction.T_DIR7;
		else
			return Direction.T_DIR8;
	}

	private Direction Get_direction( coord pre, coord cur)
	{
		int dx, dy, coord_tangent;;
		Direction result_dir;
		
		dx = cur.x - pre.x;
		dy = pre.y - cur.y;
		
		if( dx == 0)
		{
			if( dy > 0)
				result_dir = Direction.DIR1;
			else
				result_dir = Direction.DIR5;
		}
		else if( dy == 0)
		{
			if( dx > 0)
				result_dir = Direction.DIR3;
			else
				result_dir = Direction.DIR7;
		}
		else
		{
			coord_tangent = dy * 100 / dx;
			
			if( -41 <= coord_tangent && coord_tangent <= 41)
			{
				if( dx > 0)
					result_dir = Direction.DIR3;
				else
					result_dir = Direction.DIR7;
			}
			else if( 41 < coord_tangent && coord_tangent < 241)
			{
				if( dy > 0)
					result_dir = Direction.DIR2;
				else
					result_dir = Direction.DIR6;
			}
			else if( -241 < coord_tangent && coord_tangent < -41)
			{
				if( dy > 0)
					result_dir = Direction.DIR8;
				else
					result_dir = Direction.DIR4;
			}
			else
			{
				if( dy > 0)
					result_dir = Direction.DIR1;
				else
					result_dir = Direction.DIR5;
			}
		}

		return result_dir;
	}
	
	private Direction Convert_dir_to_T( Direction dir1)
	{
		if( dir1 == Direction.DIR1)
			return Direction.T_DIR1;
		else if( dir1 == Direction.DIR2)
			return Direction.T_DIR2;
		else if( dir1 == Direction.DIR3)
			return Direction.T_DIR3;
		else if( dir1 == Direction.DIR4)
			return Direction.T_DIR4;
		else if( dir1 == Direction.DIR5)
			return Direction.T_DIR5;
		else if( dir1 == Direction.DIR6)
			return Direction.T_DIR6;
		else if( dir1 == Direction.DIR7)
			return Direction.T_DIR7;
		else
			return Direction.T_DIR8;
	}
	
	private boolean is_Korean_letter( int letter_num)
	{
		if( letter_num >= 11 && letter_num <= 29)
			return true;
		else
			return false;
	}
	
	private boolean is_Num_letter( int letter_num)
	{
		if( letter_num >= 0 && letter_num <= 9)
			return true;
		else
			return false;
	}
	
	static{
		letter[0] = "0";
		letter[1] = "1";
		letter[2] = "2";
		letter[3] = "3";
		letter[4] = "4";
		letter[5] = "5";
		letter[6] = "6";
		letter[7] = "7";
		letter[8] = "8";
		letter[9] = "9";
		letter[10] = "V";
		letter[11] = "ㄱ";
		letter[12] = "ㄴ";
		letter[13] = "ㄷ";
		letter[14] = "ㄹ";
		letter[15] = "ㅁ";
		letter[16] = "ㅂ";
		letter[17] = "ㅅ";
		letter[18] = "ㅇ";
		letter[19] = "ㅈ";
		letter[20] = "ㅊ";
		letter[21] = "ㅋ";
		letter[22] = "ㅌ";
		letter[23] = "ㅍ";
		letter[24] = "ㅎ";
		letter[25] = "ㄲ";
		letter[26] = "ㄸ";
		letter[27] = "ㅃ";
		letter[28] = "ㅆ";
		letter[29] = "ㅉ";
		letter[30] = "up";
		letter[31] = "unknowncmd";
		letter[32] = "pre";
		letter[33] = "next";
		letter[34] = "startnstop";
		letter[35] = "preskip";
		letter[36] = "nextskip";
	}
}


class file_read_help_class{
	String dirs;
	LinkedList<Integer> dir_length;
	int count;
}

class coord{
	int x;
	int y;
}

enum Direction{ DIR1, DIR2, DIR3, DIR4, DIR5, DIR6, DIR7, DIR8, T_DIR1, T_DIR2, T_DIR3, T_DIR4, T_DIR5, T_DIR6, T_DIR7, T_DIR8, Z_CIRCLE}

class info_dir{
	Direction direction;
	int length;
}
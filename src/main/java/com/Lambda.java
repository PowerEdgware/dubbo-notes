package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lambda {

	public static void main(String[] args) {
		Stream<String> stream= Stream.of("I", "love", "you", "too");
//		stream.sorted((str1, str2) -> str1.length()-str2.length())
//		    .forEach(str -> System.out.println(str));
		
		//stream.sorted().forEachOrdered(s-> System.out.println(s));
		
		//Flatmap： 把stream元素打散后组成的stream,可以在对组合而成的结果stream操作。
		
		Stream<List<Integer>> allStream=Stream.of(Arrays.asList(1,2),Arrays.asList(3,4,5));
		List<Integer> allSub=allStream.flatMap(subStream-> subStream.stream())
					.collect(Collectors.toList());
		System.out.println(allSub);//[1, 2, 3, 4, 5]
		
		//折叠或者缩减操作  reduce
		
		//找出长度最长的字符串
		BinaryOperator<String> accumulator=(s1,s2)->  s1.length()>=s2.length()?s1:s2;
		Optional<String> longest=stream.reduce(accumulator);
		System.out.println(longest.orElseGet(()->""));
		
		//从Stream生成一个集合或者Map
		//collect()
		
		Stream<String> s = Stream.of("I", "love", "you", "too");
		
		//使用collect()生成Collection
		//List<String> list=s.collect(Collectors.toList());
		//Map<String, Integer> map=s.collect(Collectors.toMap(Function.identity(), String::length));
		//ArrayList<String> list=s.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		//返回具体的接口实现类类型
		//ArrayList<String> arrayList=s.collect(Collectors.toCollection(ArrayList::new));
//HashSet<String> hashSet=s.collect(Collectors.toCollection(HashSet::new));
		
		//使用collect()生成Map
		//分别指定生成key value的规则
		//Map<String, Integer> map=s.collect(Collectors.toMap(e->e, e->e.hashCode()));
		
		//使用collect()做字符串join
		//String joined=s.collect(Collectors.joining(",", "[", "]"));//[I,love,you,too]
		
		OptionalInt optionalInt=s.filter(x->x.length()>1)
			.mapToInt(String::length)
			.max();
		System.out.println(optionalInt.getAsInt());
		
		//PipelineHelper
	}
}

package org.sheedon.rrouter;

/**
 * 数据拷贝
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2021/11/2 6:02 下午
 */
public interface DataCloneable extends Cloneable{

    Object clone() throws CloneNotSupportedException;
}

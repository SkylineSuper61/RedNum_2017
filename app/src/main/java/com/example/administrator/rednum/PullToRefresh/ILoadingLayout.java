package com.example.administrator.rednum.PullToRefresh;

/**
 * Created by Administrator on 2017/6/30.
 */

/*
 * 定义用户操作和下拉/加载的状态枚举类型，以及定义 设置状态setState()，获得状态getState(),获得header/footer高度getContentSize()等方法
 */
public interface ILoadingLayout {
    /**
     * 定义用户的操作和下拉/加载的当前状态
     */
    // 枚举类型取代了以往定义常量的方式，同时枚举类型还赋予程序在编译时进行检查的功能，枚举类型本质上还是以类的形式存在
    public enum State {

        /**
         * Initial state
         */
        NONE,  // 初始化状态

        /**
         * When the UI is in a state which means that user is not interacting
         * with the Pull-to-Refresh function.
         */
        RESET,  // 未交互状态，未操作状态

        /**
         * When the UI is being pulled by the user, but has not been pulled far
         * enough so that it refreshes when released.
         */
        PULL_TO_REFRESH,  // 下拉过程中

        /**
         * When the UI is being pulled by the user, and <strong>has</strong>
         * been pulled far enough so that it will refresh when released.
         */
        RELEASE_TO_REFRESH,  // 释放即将刷新中

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        REFRESHING,  // 刷新中

        /**
         * When the UI is currently refreshing, caused by a pull gesture.
         */
        @Deprecated
        LOADING,   // 加载中

        /**
         * No more data
         */
        NO_MORE_DATA,
    }

    /**
     * 设置当前状态，派生类应该根据这个状态的变化来改变View的变化
     *
     * @param state 状态
     */
    public void setState(State state);

    /**
     * 得到当前的状态
     *
     * @return 状态
     */
    public State getState();

    /**
     * 得到当前Layout的内容大小，它将作为一个刷新的临界点
     *
     * 这个方法返回当前这个刷新Layout的大小，通常返回的是布局的高度，为了以后可以扩展为水平拉动，所以方法名字没有取成getLayoutHeight()之类的，
     * 这个返回值，将会作为松手后是否可以刷新的临界值，如果下拉的偏移值大于这个值，就认为可以刷新，否则不刷新，这个方法必须由派生类来实现。
     *
     * 此layout即为footer或header的高度
     *
     * @return 高度
     */
    public int getContentSize();

    /**
     * 在拉动时调用
     *
     * @param scale 拉动的比例
     */
    public void onPull(float scale);
}

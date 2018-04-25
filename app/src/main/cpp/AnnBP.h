//
// Created by JHanLu on 2018/3/29.
//

#include <jni.h>
#ifndef TEXTGRABBER_ANNBP_H
#define TEXTGRABBER_ANNBP_H

#define ABS(x)          (((x) > 0.0) ? (x) : (-(x)))

#define BIGRND 0x7fffffff
/*** 神经网络的数据结构。网络被假定为一个全连接的3层前向结构，
	 每层的单元0是阈值，这意味着真正的神经元编号为1－n
 ***/

typedef struct
{
    int input_n;                  /* 输入层的神经元个数 */
    int hidden_n;                 /* 隐含层的神经元个数 */
    int output_n;                 /* 输出层的神经元个数 */

    double *input_units;          /* 输入层的神经元 */
    double *hidden_units;         /* 隐藏层的神经元 */
    double *output_units;         /* 输出曾的神经元 */

    double *hidden_delta;         /* 隐藏层的误差 */
    double *output_delta;         /* 输出层的误差 */

    double *target;               /* 目标向量 */

    double **input_weights;       /* 输入层到隐藏层的连接权 */
    double **hidden_weights;      /* 隐藏层到输出曾的连接权 */

    /*** 下面两个在迭代时使用 ***/
    double **input_prev_weights;  /* 前次输入层导隐藏层权值的改变 */
    double **hidden_prev_weights; /* 前次隐藏层导输出层权值的改变 */
} BPNN;


class CAnnBP
{
public:
    void SetBParm(double eta,double momentum);

    void Initialize(int seed);
    void Read(int mfd, jlong off);
    void ReadF(char *filename);
    void Save(char *filename);
    void Identify(double *input_unit,int input_num,double *target,int target_num);
    void Train(double *input_unit,int input_num, double *target,int target_num, double *eo, double *eh);
    void FreeBP();
    void CreateBP(int n_in,int n_hidden,int n_out);
    CAnnBP();
    virtual ~CAnnBP();
    double momentum1;
    double eta1;
    BPNN* net;
    BPNN* bpnn_read(int mfd, jlong off);
    BPNN* bpnn_readf(char *filename);
    void bpnn_save(BPNN *net, char *filename);
    void bpnn_train(BPNN *net, double eta, double momentum, double *eo, double *eh);
    void bpnn_feedforward(BPNN* net);
    BPNN* bpnn_create(int n_in, int n_hidden, int n_out);
    void bpnn_free(BPNN *net);
    void bpnn_initialize(int seed);
    double* alloc_1d_dbl(int n);
    void bpnn_adjust_weights(double *delta, int ndelta, double *ly, int nly, double** w, double **oldw, double eta, double momentum);
    void bpnn_hidden_error(double* delta_h, int nh, double *delta_o, int no, double **who, double *hidden, double *err);
    void bpnn_output_error(double *delta, double *target, double *output, int nj, double *err);
    void bpnn_layerforward(double *l1, double *l2, double **conn, int n1, int n2);
    BPNN* bpnn_internal_create(int n_in, int n_hidden, int n_out);
    void bpnn_zero_weights(double **w, int m, int n);
    void bpnn_randomize_weights(double **w, int m, int n);
    double ** alloc_2d_dbl(int m, int n);
    double dpn1();
    double drnd();
    double squash(double x);
};


#endif //TEXTGRABBER_ANNBP_H

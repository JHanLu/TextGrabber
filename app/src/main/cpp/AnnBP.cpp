//
// Created by JHanLu on 2018/3/29.
//
#include <jni.h>
#include "AnnBP.h"
#include "math.h"
#include <stdio.h>
#include <stdlib.h>

#include "android/log.h"

static const char *TAG = "Ann-bp";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CAnnBP::CAnnBP()
{
    eta1=0.1;
    momentum1=0.1;

}

CAnnBP::~CAnnBP()
{
    FreeBP();
}


double CAnnBP::drnd()
{
    return ((double) rand() / (double) BIGRND);
}


/*** 返回-1.0到1.0之间的双精度随机数 ***/
double CAnnBP::dpn1()
{
    return (double) (rand())/(32767/2)-1;
}

/*** 作用函数，目前是S型函数 ***/
double CAnnBP::squash(double x)
{
    return (1.0 / (1.0 + exp(-x)));
}


/*** 申请1维双精度实数数组 ***/
double* CAnnBP::alloc_1d_dbl(int n)
{
    double *new1;

    new1 = new double[n];
    if (new1 == NULL)
    {
//		AfxMessageBox("ALLOC_1D_DBL: Couldn't allocate array of doubles\n");
        printf("ALLOC_1D_DBL: Couldn't allocate array of doubles\n");
        return (NULL);
    }
    return (new1);
}

/*** 申请2维双精度实数数组 ***/
double** CAnnBP::alloc_2d_dbl(int m, int n)
{
    int i;
    double **new1;

    new1 = new double *[m];
    if (new1 == NULL)
    {
        printf("ALLOC_2D_DBL: Couldn't allocate array of dbl ptrs\n");
        return (NULL);
    }

    for (i = 0; i < m; i++)
    {
        new1[i] = alloc_1d_dbl(n);
    }

    return (new1);
}


/*** 随机初始化权值 ***/
void CAnnBP::bpnn_randomize_weights(double **w, int m, int n)
{
    int i, j;
    for (i = 0; i <= m; i++)
    {
        for (j = 0; j <= n; j++)
        {
            w[i][j] = dpn1();
        }
    }
}

/*** 0初始化权值 ***/
void CAnnBP::bpnn_zero_weights(double **w, int m, int n)
{
    int i, j;

    for (i = 0; i <= m; i++)
    {
        for (j = 0; j <= n; j++)
        {
            w[i][j] = 0.0;
        }
    }
}

/*** 设置随机数种子 ***/
void CAnnBP::bpnn_initialize(int seed)
{
    srand(seed);
}

/*** 创建BP网络 ***/
BPNN* CAnnBP::bpnn_internal_create(int n_in, int n_hidden, int n_out)
{
    BPNN *newnet;
    newnet = new BPNN;
    /*if (newnet == NULL)
    {
        printf("BPNN_CREATE: Couldn't allocate neural network\n");
        return (NULL);
    }*/
    newnet->input_n = n_in;
    newnet->hidden_n = n_hidden;
    newnet->output_n = n_out;
    newnet->input_units = alloc_1d_dbl(n_in + 1);
    newnet->hidden_units = alloc_1d_dbl(n_hidden + 1);
    newnet->output_units = alloc_1d_dbl(n_out + 1);

    newnet->hidden_delta = alloc_1d_dbl(n_hidden + 1);
    newnet->output_delta = alloc_1d_dbl(n_out + 1);
    newnet->target = alloc_1d_dbl(n_out + 1);

    newnet->input_weights = alloc_2d_dbl(n_in + 1, n_hidden + 1);
    newnet->hidden_weights = alloc_2d_dbl(n_hidden + 1, n_out + 1);

    newnet->input_prev_weights = alloc_2d_dbl(n_in + 1, n_hidden + 1);
    newnet->hidden_prev_weights = alloc_2d_dbl(n_hidden + 1, n_out + 1);
    return (newnet);

}

/* 释放BP网络所占地内存空间 */
void CAnnBP::bpnn_free(BPNN *net)
{
    int n1, n2, i;

    n1 = net->input_n;
    n2 = net->hidden_n;

    delete [] net->input_units;
    delete [] net->hidden_units;
    delete [] net->output_units;

    delete [] net->hidden_delta;
    delete [] net->output_delta;
    delete [] net->target;

    for (i = 0; i <= n1; i++)
    {
        delete [] net->input_weights[i];
        delete [] net->input_prev_weights[i];
    }
    delete [] net->input_weights;
    delete [] net->input_prev_weights;

    for (i = 0; i <= n2; i++)
    {
        delete [] net->hidden_weights[i];
        delete [] net->hidden_prev_weights[i];
    }
    delete [] net->hidden_weights;
    delete [] net->hidden_prev_weights;

    delete [] net;
}


/*** 创建一个BP网络，并初始化权值***/
BPNN* CAnnBP::bpnn_create(int n_in, int n_hidden, int n_out)
{
    BPNN *newnet;

    newnet = bpnn_internal_create(n_in, n_hidden, n_out);

#ifdef INITZERO
    bpnn_zero_weights(newnet->input_weights, n_in, n_hidden);
#else
    bpnn_randomize_weights(newnet->input_weights, n_in, n_hidden);
#endif
    bpnn_randomize_weights(newnet->hidden_weights, n_hidden, n_out);
    bpnn_zero_weights(newnet->input_prev_weights, n_in, n_hidden);
    bpnn_zero_weights(newnet->hidden_prev_weights, n_hidden, n_out);

    return (newnet);

}



void CAnnBP::bpnn_layerforward(double *l1, double *l2, double **conn, int n1, int n2)
{
    double sum;
    int j, k;

    /*** 设置阈值 ***/
    l1[0] = 1.0;

    /*** 对于第二层的每个神经元 ***/
    for (j = 1; j <= n2; j++) {

        /*** 计算输入的加权总和 ***/
        sum = 0.0;
        for (k = 0; k <= n1; k++) {
            sum += conn[k][j] * l1[k];
        }
        l2[j] = squash(sum);
    }
}

/* 输出误差 */
void CAnnBP::bpnn_output_error(double *delta, double *target, double *output, int nj, double *err)
{
    int j;
    double o, t, errsum;

    errsum = 0.0;
    for (j = 1; j <= nj; j++) {
        o = output[j];
        t = target[j];
        delta[j] = o * (1.0 - o) * (t - o);
        errsum += ABS(delta[j]);
    }
    *err = errsum;

}

/* 隐含层误差 */
void CAnnBP::bpnn_hidden_error(double *delta_h, int nh, double *delta_o, int no, double **who, double *hidden, double *err)
{
    int j, k;
    double h, sum, errsum;

    errsum = 0.0;
    for (j = 1; j <= nh; j++) {
        h = hidden[j];
        sum = 0.0;
        for (k = 1; k <= no; k++) {
            sum += delta_o[k] * who[j][k];
        }
        delta_h[j] = h * (1.0 - h) * sum;
        errsum += ABS(delta_h[j]);
    }
    *err = errsum;
}

/* 调整权值 */
void CAnnBP::bpnn_adjust_weights(double *delta, int ndelta, double *ly, int nly, double **w, double **oldw, double eta, double momentum)
{
    double new_dw;
    int k, j;

    ly[0] = 1.0;
    for (j = 1; j <= ndelta; j++) {
        for (k = 0; k <= nly; k++) {
            new_dw = ((eta * delta[j] * ly[k]) + (momentum * oldw[k][j]));
            w[k][j] += new_dw;
            oldw[k][j] = new_dw;
        }
    }

}

/* 进行前向运算 */
void CAnnBP::bpnn_feedforward(BPNN *net)
{
    int in, hid, out;

    in = net->input_n;
    hid = net->hidden_n;
    out = net->output_n;

    /*** Feed forward input activations. ***/
    bpnn_layerforward(net->input_units, net->hidden_units, net->input_weights, in, hid);
    bpnn_layerforward(net->hidden_units, net->output_units, net->hidden_weights, hid, out);
}

/* 训练BP网络 */
void CAnnBP::bpnn_train(BPNN *net, double eta, double momentum, double *eo, double *eh)
{
    int in, hid, out;
    double out_err, hid_err;

    in = net->input_n;
    hid = net->hidden_n;
    out = net->output_n;

    /*** 前向输入激活 ***/
    bpnn_layerforward(net->input_units, net->hidden_units,
                      net->input_weights, in, hid);
    bpnn_layerforward(net->hidden_units, net->output_units,
                      net->hidden_weights, hid, out);

    /*** 计算隐含层和输出层误差 ***/
    bpnn_output_error(net->output_delta, net->target, net->output_units,
                      out, &out_err);
    bpnn_hidden_error(net->hidden_delta, hid, net->output_delta, out,
                      net->hidden_weights, net->hidden_units, &hid_err);
    *eo = out_err;
    *eh = hid_err;

    /*** 调整输入层和隐含层权值 ***/
    bpnn_adjust_weights(net->output_delta, out, net->hidden_units, hid,
                        net->hidden_weights, net->hidden_prev_weights, eta, momentum);
    bpnn_adjust_weights(net->hidden_delta, hid, net->input_units, in,
                        net->input_weights, net->input_prev_weights, eta, momentum);
}

/* 保存BP网络 */
void CAnnBP::bpnn_save(BPNN *net, char *filename)
{
    FILE *fp;
    fp = fopen(filename, "w");

    int n1, n2, n3, i, j;
    double **w;

    n1 = net->input_n;  n2 = net->hidden_n;  n3 = net->output_n;

    fprintf(fp, "%d\n", n1);
    fprintf(fp, "%d\n", n2);
    fprintf(fp, "%d\n", n3);
    w = net->input_weights;
    for (i = 0; i <= n1; i++)
    {
        for (j = 0; j <= n2; j++)
        {
            fprintf(fp, "%lf\n", w[i][j]);
        }
    }

    w = net->hidden_weights;
    for (i = 0; i <= n2; i++)
    {
        for (j=0; j <= n3; j++)
        {
            fprintf(fp, "%lf\n", w[i][j]);
        }
    }

    fclose(fp);
}

/* 从文件中读取BP网络 */
BPNN* CAnnBP::bpnn_read(int mfd, jlong off)
{


    BPNN *new1;
    int n1, n2, n3, i, j;

    FILE *fp;
    fp = fdopen(mfd, "r");
    if(NULL == fp) {
        LOGE("file open error");
    }
    fseek(fp, off, SEEK_SET);
    fscanf(fp, "%d\n", &n1);
    fscanf(fp, "%d\n", &n2);
    fscanf(fp, "%d\n", &n3);
    LOGD("%d,%d,%d", n1, n2, n3);
    new1 = bpnn_internal_create(n1, n2, n3);
    for (i = 0; i <= n1; i++)
    {
        for (j = 0; j <= n2; j++)
        {
            fscanf(fp, "%lf\n", &new1->input_weights[i][j]);
        }
    }

    for (i = 0; i <= n2; i++)
    {
        for (j = 0; j <= n3; j++)
        {
            fscanf(fp, "%lf\n", &new1->hidden_weights[i][j]);
        }
    }

    fclose(fp);

    bpnn_zero_weights(new1->input_prev_weights, n1, n2);
    bpnn_zero_weights(new1->hidden_prev_weights, n2, n3);
    return (new1);
}

BPNN* CAnnBP::bpnn_readf(char *filename)
{
    BPNN *new1;
    int n1=728, n2=300, n3=6, i, j;

    FILE *fp;
    fp = fopen(filename, "r");
    fscanf(fp, "%d\n", &n1);
    fscanf(fp, "%d\n", &n2);
    fscanf(fp, "%d\n", &n3);
    new1 = bpnn_internal_create(n1, n2, n3);

    for (i = 0; i <= n1; i++)
    {
        for (j = 0; j <= n2; j++)
        {
            fscanf(fp, "%lf\n", &new1->input_weights[i][j]);
        }
    }

    for (i = 0; i <= n2; i++)
    {
        for (j = 0; j <= n3; j++)
        {
            fscanf(fp, "%lf\n", &new1->hidden_weights[i][j]);
        }
    }

    fclose(fp);

    bpnn_zero_weights(new1->input_prev_weights, n1, n2);
    bpnn_zero_weights(new1->hidden_prev_weights, n2, n3);

    return (new1);
}

void CAnnBP::CreateBP(int n_in, int n_hidden, int n_out)
{
    net=bpnn_create(n_in,n_hidden,n_out);
}

void CAnnBP::FreeBP()
{
    bpnn_free(net);

}

void CAnnBP::Train(double *input_unit,int input_num, double *target,int target_num, double *eo, double *eh)
{
    for(int i=1;i<=input_num;i++)
    {
        net->input_units[i]=input_unit[i-1];
    }

    for(int j=1;j<=target_num;j++)
    {
        net->target[j]=target[j-1];
    }
    bpnn_train(net,eta1,momentum1,eo,eh);

}

void CAnnBP::Identify(double *input_unit,int input_num,double *target,int target_num)
{
    for(int i=1;i<=input_num;i++)
    {
        net->input_units[i]=input_unit[i-1];
    }
    bpnn_feedforward(net);
    for(int j=1;j<=target_num;j++)
    {
        target[j-1]=net->output_units[j];
    }
}

void CAnnBP::Save(char *filename)
{
    bpnn_save(net,filename);

}

void CAnnBP::Read(int mfd,jlong off)
{
    net=bpnn_read(mfd, off);
}

void CAnnBP::ReadF(char *filename)
{
    net=bpnn_readf(filename);
}

void CAnnBP::SetBParm(double eta, double momentum)
{
    eta1=eta;
    momentum1=momentum;

}

void CAnnBP::Initialize(int seed)
{
    bpnn_initialize(seed);

}


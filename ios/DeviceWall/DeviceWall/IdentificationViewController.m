//
//  IdentificationViewController.m
//  DeviceWall
//
//  Created by DX050 on 2013-03-11.
//  Copyright (c) 2013 Xtreme Labs. All rights reserved.
//

#import "IdentificationViewController.h"
#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]


@interface IdentificationViewController (){
    UILabel* lblId;
}

@end

@implementation IdentificationViewController

- (id)init
{
    self = [super init];
    if (self) {

        
    }
    return self;
}
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.wantsFullScreenLayout = YES;
    [self.view setBackgroundColor:[UIColor redColor]];
    UITapGestureRecognizer* gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapped)];
    [self.view addGestureRecognizer:gestureRecognizer];
    [[UIApplication sharedApplication] setStatusBarHidden:YES];
    lblId = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 480)];
    [lblId setTextAlignment:NSTextAlignmentCenter];
    [lblId setBackgroundColor:[UIColor clearColor]];

    if(self.deviceId){
    [lblId setText:[NSString stringWithFormat:@"%d",self.deviceId]];
    }
    else{
    [lblId setText:@"test"];
    }
    [self.view addSubview:lblId];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) tapped{
    NSLog(@"Tapped");
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    [self.modalDelegate dismissModalView];
}

@end

//
//  ViewController.m
//  DeviceWall
//
//  Created by DX050 on 2013-03-11.
//  Copyright (c) 2013 Xtreme Labs. All rights reserved.
//

#import "ViewController.h"
#import "IdentificationViewController.h"
#import "AppDelegate.h"
#import "AFJSONRequestOperation.h"
@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)setupButtonPressed:(id)sender {
    NSString* serverAddress = @"http://192.168.91.179:9999";
	NSString* init = @"/init";
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    CGFloat screenWidth = screenRect.size.width;
    CGFloat screenHeight = screenRect.size.height;
    NSLog(@"setup button pressed");
    NSString* url = [NSString stringWithFormat:@"%@%@?width=%f&height=%f",serverAddress, init, screenWidth, screenHeight];
    NSURL* requestURL = [NSURL URLWithString:url];
    NSMutableURLRequest* request = [[NSMutableURLRequest alloc] initWithURL:requestURL];
    AFJSONRequestOperation* operations = [AFJSONRequestOperation JSONRequestOperationWithRequest:request success:^(NSURLRequest *request, NSHTTPURLResponse *response, id JSON) {
        NSDictionary* responseDict = (NSDictionary*)JSON;
        NSInteger deviceId = [[responseDict objectForKey:@"id"] integerValue];
        IdentificationViewController* identViewController = [[IdentificationViewController alloc] init];
        [identViewController setModalDelegate:self];
        [identViewController setDeviceId:deviceId];
        [self.navigationController presentModalViewController:identViewController animated:YES];
    } failure:^(NSURLRequest *request, NSHTTPURLResponse *response, NSError *error, id JSON) {
        NSLog(@"error");
    }];
    [operations start];
//    NSURL *addressUrl =
//    NSMutableURLRequest *request = [Utility getMutableURLRequest:addressUrl];
//    AFJSONRequestOperation* 
//    IdentificationViewController* identViewController = [[IdentificationViewController alloc] init];
//    [identViewController setModalDelegate:self];
//    [self.navigationController presentModalViewController:identViewController animated:YES];
    NSLog(@"url:%@", url);
}


- (void)dismissModalView{
    [self dismissViewControllerAnimated:YES completion:^{
        
    }];
}
@end

//
//  IdentificationViewController.h
//  DeviceWall
//
//  Created by DX050 on 2013-03-11.
//  Copyright (c) 2013 Xtreme Labs. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol ModalDelegate <NSObject>

- (void)dismissModalView;

@end

@interface IdentificationViewController : UIViewController
@property (nonatomic, weak) id<ModalDelegate> modalDelegate;
@property (nonatomic) NSInteger deviceId;
@end

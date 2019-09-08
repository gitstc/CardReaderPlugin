//
//  CardReaderPlugin.h
//  MagCardReader
//
//  Created by apple on 3/19/14.
//  Copyright (c) 2014 ___FULLUSERNAME___. All rights reserved.
//

#import <Cordova/CDVPlugin.h>
#import "iMagRead.h"

@interface CardReaderPlugin : CDVPlugin {
    NSString* callbackId;
    iMagRead* iMagReader;
}

@property (nonatomic, copy) NSString* callbackId;

- (void) initReader:(CDVInvokedUrlCommand*)command;
- (void) startReader:(CDVInvokedUrlCommand*)command;
- (void) stopReader:(CDVInvokedUrlCommand*)command;
@end
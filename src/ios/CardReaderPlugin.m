/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/*
 NOTE: plugman/cordova cli should have already installed this,
 but you need the value UIViewControllerBasedStatusBarAppearance
 in your Info.plist as well to set the styles in iOS 7
 */

#import <MediaPlayer/MediaPlayer.h>
#import <Cordova/CDV.h>
#import "CardReaderPlugin.h"

@interface CardReaderPlugin ()

@end

@implementation CardReaderPlugin

@synthesize callbackId;

bool hasStarted = NO;

- (void) initReader:(CDVInvokedUrlCommand *)command{
    NSLog(@"Initializing Card Reader!");
    
    self.callbackId = command.callbackId;
    
    @try{
        iMagReader = [[iMagRead alloc] init];
        [iMagReader Start];
        hasStarted = YES;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(UpdateBytes:) name:CARREAD_MSG_ByteUpdate object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(UpdateBits:) name:CARREAD_MSG_BitUpdate object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(UpdateError:) name:CARREAD_MSG_Err object:nil];
        
        CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@""]];
        [self.webView stringByEvaluatingJavaScriptFromString:[result toSuccessCallbackString: self.callbackId]];
    }
    @catch(NSException *ex){
        CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"Exception: %@",ex]];
        [self.webView stringByEvaluatingJavaScriptFromString:[result toErrorCallbackString: self.callbackId]];
    }
}

- (void)UpdateBytes:(NSNotification*)aNotification
{
    NSString* str = [aNotification object];
    [self performSelectorOnMainThread:@selector(updateBytCtl:) withObject:str waitUntilDone:YES];
}

- (void)UpdateBits:(NSNotification*)aNotification
{
    NSString* str = [aNotification object];
    [self performSelectorOnMainThread:@selector(updateBitCtl:) withObject:str waitUntilDone:YES];
}

- (void)UpdateError:(NSNotification*)aNotification
{
    NSString* str = [aNotification object];
    [self performSelectorOnMainThread:@selector(updateErrorState:) withObject:str waitUntilDone:YES];
}

- (void)updateBytCtl:(NSString*)text
{
    if(![text isEqualToString:@"Data Parse Error"]){
        [self writeJavascript:[NSString stringWithFormat:@"CardReaderPlugin.onSwipeDataReceived('%@')",text]];
    }
    else{
        [self writeJavascript:@"CardReaderPlugin.onSwipeDataReceived('Bad Read')"];
    }
}
- (void)updateBitCtl:(NSString*)text
{
}
- (void)updateErrorState:(NSString*)text
{
}

- (void)startReader:(CDVInvokedUrlCommand *)command{
	self.callbackId = command.callbackId;

    if(hasStarted == NO){
        @try{
            [iMagReader Start];
            hasStarted = YES;
            CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@""]];
            [self.webView stringByEvaluatingJavaScriptFromString:[result toSuccessCallbackString: self.callbackId]];
        }
        @catch(NSException *ex){
            CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@",ex]];
            [self.webView stringByEvaluatingJavaScriptFromString:[result toErrorCallbackString: self.callbackId]];
        }
    }
}

- (void)stopReader:(CDVInvokedUrlCommand *)command{
	self.callbackId = command.callbackId;

    if(hasStarted == YES){
        @try{
            [iMagReader Stop];
            hasStarted = NO;
            CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@""]];
            [self.webView stringByEvaluatingJavaScriptFromString:[result toSuccessCallbackString: self.callbackId]];
        }
        @catch(NSException *ex){
            CDVPluginResult* result = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@",ex]];
            [self.webView stringByEvaluatingJavaScriptFromString:[result toErrorCallbackString: self.callbackId]];
        }
    }
}

@end

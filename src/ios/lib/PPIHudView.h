//
//  PPIHudView.h
//  PayecoDemoDev
//
//  Created by 詹海岛 on 15/2/12.
//  Copyright (c) 2015年 PayEco. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PPIHudView : UIView
{
    UIView *contentView_;
}

- (id)initWithTitle:(NSString *)title parent:(UIView *)parent;
- (void)show;
- (void)dismiss;

@end

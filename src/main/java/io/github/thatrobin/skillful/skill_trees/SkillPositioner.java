package io.github.thatrobin.skillful.skill_trees;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkillPositioner {
    private final Skill skill;
    @Nullable
    private final SkillPositioner parent;
    @Nullable
    private final SkillPositioner previousSibling;
    private final int childrenSize;
    private final List<SkillPositioner> children = Lists.newArrayList();
    private SkillPositioner optionalLast;
    @Nullable
    private SkillPositioner substituteChild;
    private int depth;
    private float row;
    private float relativeRowInSiblings;
    private float field_1266;
    private float field_1265;

    public SkillPositioner(Skill skill, @Nullable SkillPositioner parent, @Nullable SkillPositioner previousSibling, int childrenSize, int depth) {
        if (skill.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position an invisible Skill!");
        }
        this.skill = skill;
        this.parent = parent;
        this.previousSibling = previousSibling;
        this.childrenSize = childrenSize;
        this.optionalLast = this;
        this.depth = depth;
        this.row = -1.0f;
        SkillPositioner skillPositioner = null;
        for (Skill skill2 : skill.getChildren()) {
            skillPositioner = this.findChildrenRecursively(skill2, skillPositioner);
        }
    }

    @Nullable
    private SkillPositioner findChildrenRecursively(Skill skill, @Nullable SkillPositioner lastChild) {
        if (skill.getDisplay() != null) {
            lastChild = new SkillPositioner(skill, this, lastChild, this.children.size() + 1, this.depth + 1);
            this.children.add(lastChild);
        } else {
            for (Skill skill2 : skill.getChildren()) {
                lastChild = this.findChildrenRecursively(skill2, lastChild);
            }
        }
        return lastChild;
    }

    private void calculateRecursively() {
        if (this.children.isEmpty()) {
            this.row = this.previousSibling != null ? this.previousSibling.row + 1.0f : 0.0f;
            return;
        }
        SkillPositioner skillPositioner = null;
        for (SkillPositioner skillPositioner2 : this.children) {
            skillPositioner2.calculateRecursively();
            skillPositioner = skillPositioner2.onFinishCalculation(skillPositioner == null ? skillPositioner2 : skillPositioner);
        }
        this.onFinishChildrenCalculation();
        float f = (this.children.get(0).row + this.children.get(this.children.size() - 1).row) / 2.0f;
        if (this.previousSibling != null) {
            this.row = this.previousSibling.row + 1.0f;
            this.relativeRowInSiblings = this.row - f;
        } else {
            this.row = f;
        }
    }

    private float findMinRowRecursively(float deltaRow, int depth, float minRow) {
        this.row += deltaRow;
        this.depth = depth;
        if (this.row < minRow) {
            minRow = this.row;
        }
        for (SkillPositioner skillPositioner : this.children) {
            minRow = skillPositioner.findMinRowRecursively(deltaRow + this.relativeRowInSiblings, depth + 1, minRow);
        }
        return minRow;
    }

    private void increaseRowRecursively(float deltaRow) {
        this.row += deltaRow;
        for (SkillPositioner skillPositioner : this.children) {
            skillPositioner.increaseRowRecursively(deltaRow);
        }
    }

    private void onFinishChildrenCalculation() {
        float f = 0.0f;
        float g = 0.0f;
        for (int i = this.children.size() - 1; i >= 0; --i) {
            SkillPositioner skillPositioner = this.children.get(i);
            skillPositioner.row += f;
            skillPositioner.relativeRowInSiblings += f;
            f += skillPositioner.field_1265 + (g += skillPositioner.field_1266);
        }
    }

    @Nullable
    private SkillPositioner getFirstChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    @Nullable
    private SkillPositioner getLastChild() {
        if (this.substituteChild != null) {
            return this.substituteChild;
        }
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    private SkillPositioner onFinishCalculation(SkillPositioner last) {
        if (this.previousSibling == null) {
            return last;
        }
        SkillPositioner skillPositioner = this;
        SkillPositioner skillPositioner2 = this;
        SkillPositioner skillPositioner3 = this.previousSibling;
        if(this.parent != null) {
            SkillPositioner skillPositioner4 = this.parent.children.get(0);
            float f = this.relativeRowInSiblings;
            float g = this.relativeRowInSiblings;
            float h = skillPositioner3.relativeRowInSiblings;
            float i = skillPositioner4.relativeRowInSiblings;
            while (skillPositioner3.getLastChild() != null && skillPositioner.getFirstChild() != null) {
                skillPositioner3 = skillPositioner3.getLastChild();
                skillPositioner = skillPositioner.getFirstChild();
                skillPositioner4 = skillPositioner4.getFirstChild();
                skillPositioner2 = skillPositioner2.getLastChild();
                assert skillPositioner2 != null;
                skillPositioner2.optionalLast = this;
                float j = skillPositioner3.row + h - (skillPositioner.row + f) + 1.0f;
                if (j > 0.0f) {
                    skillPositioner3.getLast(this, last).pushDown(this, j);
                    f += j;
                    g += j;
                }
                h += skillPositioner3.relativeRowInSiblings;
                f += skillPositioner.relativeRowInSiblings;
                assert skillPositioner4 != null;
                i += skillPositioner4.relativeRowInSiblings;
                g += skillPositioner2.relativeRowInSiblings;
            }
            if (skillPositioner3.getLastChild() != null && skillPositioner2.getLastChild() == null) {
                skillPositioner2.substituteChild = skillPositioner3.getLastChild();
                skillPositioner2.relativeRowInSiblings += h - g;
            } else {
                if (skillPositioner.getFirstChild() != null && skillPositioner4.getFirstChild() == null) {
                    skillPositioner4.substituteChild = skillPositioner.getFirstChild();
                    skillPositioner4.relativeRowInSiblings += f - i;
                }
                last = this;
            }
        }
        return last;
    }

    private void pushDown(SkillPositioner positioner, float extraRowDistance) {
        float f = positioner.childrenSize - this.childrenSize;
        if (f != 0.0f) {
            positioner.field_1266 -= extraRowDistance / f;
            this.field_1266 += extraRowDistance / f;
        }
        positioner.field_1265 += extraRowDistance;
        positioner.row += extraRowDistance;
        positioner.relativeRowInSiblings += extraRowDistance;
    }

    private SkillPositioner getLast(SkillPositioner skillPositioner, SkillPositioner skillPositioner2) {
        if(skillPositioner.parent != null) {
            if (this.optionalLast != null && skillPositioner.parent.children.contains(this.optionalLast)) {
                return this.optionalLast;
            }
        }
        return skillPositioner2;
    }

    private void apply() {
        if (this.skill.getDisplay() != null) {
            this.skill.getDisplay().setPos(this.depth, this.row);
        }
        if (!this.children.isEmpty()) {
            for (SkillPositioner skillPositioner : this.children) {
                skillPositioner.apply();
            }
        }
    }

    public static void arrangeForTree(Skill root) {
        if (root.getDisplay() == null) {
            throw new IllegalArgumentException("Can't position children of an invisible root!");
        }
        SkillPositioner skillPositioner = new SkillPositioner(root, null, null, 1, 0);
        skillPositioner.calculateRecursively();
        float f = skillPositioner.findMinRowRecursively(0.0f, 0, skillPositioner.row);
        if (f < 0.0f) {
            skillPositioner.increaseRowRecursively(-f);
        }
        skillPositioner.apply();
    }
}


